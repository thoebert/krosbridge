import org.slf4j.bridge.SLF4JBridgeHandler

fun setupLogging(){
    SLF4JBridgeHandler.removeHandlersForRootLogger()  // Optionally remove existing handlers attached to j.u.l root logge
    SLF4JBridgeHandler.install() // add SLF4JBridgeHandler to j.u.l's root logger

}