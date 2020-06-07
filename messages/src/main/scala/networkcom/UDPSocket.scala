package networkcom

trait UDPSocket {

  def peer: Peer

  def bind(port: Int, address: Option[String] = None): Unit

  def send(message: Array[Byte], port: Int, address: String): Unit

  protected var _onMessage: (Array[Byte], Peer) => Unit = (_, _) => {}

  def setMessageCallback(callback: (Array[Byte], Peer) => Unit): Unit

  def onMessage: (Array[Byte], Peer) => Unit = _onMessage

  protected var _onClose: () => Unit = () => {}

  def setCloseCallback(callback: () => Unit): Unit

  def onClose: () => Unit = _onClose

  protected var _onListening: () => Unit = () => {}

  def setListeningCallback(callback: () => Unit): Unit

  def onListening: () => Unit = _onListening

  def close(): Unit

}
