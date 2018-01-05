package networkcom

import sharednodejsapis._

import scala.collection.mutable
import scala.scalajs.js.timers._

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

  private val socket: Socket = DgramModule.createSocket(t)


  socket.on("listening", () => {
    val address = socket.address()
    println(s"server listening ${address.address}:${address.port}")
  })

  socket.on("close", () => println("server disconnected"))

  socket.on("error", (err: ErrorEvent) => {
    println("server error:")
    println(s"${err.stack}")
    socket.close()
  })

  socket.on("message", (msg: Buffer, rInfo: RInfo) => {
    val peer = Peer(rInfo.address, rInfo.port)

    clientConnections.get(peer) match {
      case Some(connection) => connection.onMessage(msg)
      case None =>
        val connection = new Connection(peer, socket, msg => onMessage(peer, msg))
        clientConnections += ((peer, connection))
        connection.onMessage(msg)
    }
  })

  def activate(): Unit = {
    if (address == "*") socket.bind(port) else socket.bind(port, address)

    // checking every 10s which connected client is still alive
    setInterval(10000)(checkingClientStatus())
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
    clientConnections(peer).sendNormal(Disconnected())
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
