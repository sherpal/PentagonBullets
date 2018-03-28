package networkcom

import networkcom.tablemessages.{Hello, HolePunching}

import scala.collection.mutable

/**
 * A Server is a more evolved Node.js Socket that has [[Client]]s connected to it.
 *
 * There are four types of messages that can be sent to the Clients:
 * - disordered, unreliable messages (no guarantee that message arrives, and don't watch the order in which they are
 *   sent
 * - disordered, reliable messages (guarantee to arrive)
 * - ordered, unreliable messages (no guarantee that the message arrives, and if a message emitted earlier arrives after
 *   a message emitted later, it is discarded)
 * - ordered, reliable (guarantee to arrive in the order they are emitted)
 *
 * address         the address to bind the server to (if address = "*", receives from all addresses)
 * port            the port to listen to
 * t               see Node.js Socket docs
 * messageCallback callback function called when a [[Message]] is received. Two arguments are the server instance
 *                        and the Message received. We use boopickle to manage message encoding
 * clientConnectedCallback callback function called when a [[Client]] connects or disconnects.
 * debug           the server is a little bit more verbose if debug is true
 */
abstract class Server extends UDPNode {

  val address: String

  val port: Int

  val t: String = "udp4"

  val debug: Boolean = false

  def messageCallback(server: Server, peer: Peer, msg: Message): Unit

  def clientConnectedCallback(server: Server, peer: Peer, connected: Boolean): Unit

  private val clientConnections: mutable.Map[Peer, Connection] = mutable.Map()

  private val socket: UDPSocket = PlatformDependent.createSocket()

  socket.setMessageCallback((array: Array[Byte], peer: Peer) => {
    clientConnections.get(peer) match {
      case Some(connection) =>
        connection.onMessage(array)
      case None =>
        val connection = makeConnection(peer)
        connection.onMessage(array)
    }
  })

  socket.setCloseCallback(() => println("server disconnected"))

  socket.setListeningCallback(() => {
    val peer = socket.peer
    println(s"server listening ${peer.address}:${peer.port}")
  })

  /**
   * Manually creates a Connection with the peer.
   * This is used when a new socket sent a message, and when making a UDP hole punching.
   *
   * The ability to make UDP hole punching is the reason why this method is public (I guess).
   */
  def makeConnection(peer: Peer): Connection = {
    val connection = clientConnections.getOrElse(peer, new Connection(peer, socket, msg => onMessage(peer, msg)))
    clientConnections += peer -> connection

    connection
  }

  def removeConnection(peer: Peer): Unit = {
    clientConnections -= peer
  }

  def pushConnection(peer: Peer): Unit = {
    var counter = 0

    def loop(): Unit = {
      if (counter < 20) {
        makeConnection(peer)

        sendNormal(Hello(""), peer)

        counter += 1

        PlatformDependent.setTimeout(1000)(loop())
      }
    }
    loop()
  }

  def activate(): Unit = {
    if (address == "*") socket.bind(port) else socket.bind(port, Some(address))

    // checking every 10s which connected client is still alive
    PlatformDependent.setInterval(10000)(checkingClientStatus())
  }

  def broadcastNormal(msg: Message): Unit =
    clientConnections.valuesIterator.foreach(_.sendNormal(msg))

  def broadcastOrdered(msg: Message): Unit =
    clientConnections.valuesIterator.foreach(_.sendOrdered(msg))

  def broadcastReliable(msg: Message): Unit =
    clientConnections.valuesIterator.foreach(_.sendReliable(msg))

  def broadcastOrderedReliable(msg: Message): Unit =
    clientConnections.valuesIterator.foreach(_.sendOrderedReliable(msg))

  def sendNormal(msg: Message, peer: Peer): Unit =
    clientConnections(peer).sendNormal(msg)

  def sendOrdered(msg: Message, peer: Peer): Unit =
    clientConnections(peer).sendOrdered(msg)

  def sendReliable(msg: Message, peer: Peer): Unit = {
    clientConnections.get(peer) match {
      case Some(connection) => connection.sendReliable(msg)
      case _ =>
    }
  }

  def sendOrderedReliable(msg: Message, peer: Peer): Unit =
    clientConnections(peer).sendOrderedReliable(msg)


  private def checkingClientStatus(): Unit = {
    if (debug){
      println("checking live clients status...")
      println(s"${clientConnections.size} were connected last time")
    }

    // gently disconnecting all dead clients
    clientConnections.filter(!_._2.isConnected).foreach(elem => clientDisconnect(elem._1))

    if (debug) println(s"${clientConnections.size} are still connected")
  }

  private def onMessage(peer: Peer, msg: Message): Unit = {
    msg match {
      case Connect() => clientConnect(peer)
      case Disconnect() => clientDisconnect(peer)
      case _ => messageCallback(this, peer, msg)
    }
  }

  private def clientConnect(peer: Peer): Unit = {
    clientConnections(peer).sendReliable(Connected())
    clientConnectedCallback(this, peer, connected = true)
    if (debug) {
      println("New client connected")
      clientConnections.foreach(println(_))
    }
  }

  private def clientDisconnect(peer: Peer): Unit = {
    //clientConnections(peer).sendNormal(Disconnected())
    clientConnections -= peer

    clientConnectedCallback(this, peer, connected = false)
    if (debug) println("Client disconnected")
  }

  def clients: Set[Peer] = clientConnections.keys.toSet

  def disconnect(): Unit = {
    broadcastNormal(Disconnected())
    socket.close()
  }

}
