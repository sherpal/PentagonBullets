package networkcom


/**
 * A Client is a more evolved Node.js Socket that connects to a [[Server]] in order to communicate.
 *
 * See details about messages in [[Server]] doc.
 *
 * address           the address of the server (can be "localhost").
 * port              the port the Server is listening to.
 * t                 see Node.js Socket docs.
 * messageCallback   callback function called when a [[Message]] is received. Two arguments are the Client
 *                          instance and the Message received. We use boopickle to manage message encoding
 * connectedCallback callback function called when the Client is connected or disconnected.
 */
abstract class Client extends UDPNode {

  val address: String

  val port: Int

  val t: String = "udp4"

  def messageCallback(client: Client, msg: Message): Unit

  def connectedCallback(client: Client, peer: Peer, connected: Boolean): Unit


  private var connected = false
  def isConnected: Boolean = connected

  private val socket: UDPSocket = PlatformDependent.createSocket()

  //private val socket: Socket = DgramModule.createSocket(t)

  private val connection: Connection = new Connection(Peer(address, port), socket, onMessage)

  def latency: Int = connection.latency

  // The latency is computed via averaging of the recorded latencies
  def meanLatency: Int = connection.meanLatency

  def activatePing(delay: Int = 1000): Unit =
    connection.activatePing(delay)

  def computeLinkTime(sampleTime: Int = 100, sampleNumber: Int = 20, endCallback: (Long) => Any): Unit = {
    connection.computeLinkTime(sampleTime, sampleNumber, endCallback)
  }



//  socket.on("error", (err: ErrorEvent) => {
//    println("server error:")
//    println(s"${err.stack}")
//    socket.close()
//    changeConnectedStatus(false)
//  })

  socket.setCloseCallback(() => changeConnectedStatus(false))

  //socket.on("close", () => changeConnectedStatus(false))

  socket.setMessageCallback((array: Array[Byte], _: Peer) => connection.onMessage(array))

  //socket.on("message", (msg: Buffer, _: RInfo) => connection.onMessage(msg))

  private def onMessage(msg: Message): Unit = {
    msg match {
      case Connected() => changeConnectedStatus(true)
      case Disconnected() => changeConnectedStatus(false)
      case _ if connected => messageCallback(this, msg)
    }
  }

  private def changeConnectedStatus(connectedStatus: Boolean): Unit = {
    if (connected != connectedStatus) {
      if (connectedStatus) {
        connected = true
        connectedCallback(this, Peer(address, port), connected = true)
        connection.activatePing()
      }
      else {
        connection.deactivatePing()
        connected = false
        connectedCallback(this, Peer(address, port), connected = false)
      }
    }
  }

  def connect(): Unit =
    connection.sendReliable(Connect())

  def disconnect(): Unit = {
    if (connected) {
      connection.sendNormal(Disconnect())
      changeConnectedStatus(false)
    }
  }


  def sendNormal(msg: Message): Unit =
    if (connected) connection.sendNormal(msg)

  def sendOrdered(msg: Message): Unit =
    if (connected) connection.sendOrdered(msg)

  def sendReliable(msg: Message): Unit =
    if (connected) connection.sendReliable(msg)

  def sendOrderedReliable(msg: Message): Unit =
    if (connected) connection.sendOrderedReliable(msg)

  //    val buffer = Pickle.intoBytes(dateObject.getTime)
//    val data = Array.ofDim[Byte](buffer.remaining())
//    buffer.get(data)
//    println(data)
//    println(data.getClass)

}
