import java.io.File;
import java.net.InetSocketAddress;
import java.net.URL;
import java.security.ProtectionDomain;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyLauncher {
    public static void main(String[] args) throws Exception {
        int port = 10080;
        Server server = new Server(new InetSocketAddress(port));
        WebAppContext context = new WebAppContext();

        File home = getWaldapHome();
        if (!home.exists()){
            home.mkdirs();
        }

        File webTmpDir = new File(getWaldapHome(), "tmp");
        context.setTempDirectory(webTmpDir);

        ProtectionDomain domain = JettyLauncher.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        context.setContextPath("/");
        context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        context.setServer(server);
        context.setWar(location.toExternalForm());
        Handler handler = addStatisticsHandler(context);
        server.setHandler(handler);

        server.setStopAtShutdown(true);
        server.setStopTimeout(7000);
        server.start();

        server.join();
    }

    private static File getWaldapHome(){
        String home = System.getProperty("waldap.home");
        if(home != null && home.length() > 0){
            return new File(home);
        }
        home = System.getenv("WALDAP_HOME");
        if(home != null && home.length() > 0){
            return new File(home);
        }
        return new File(System.getProperty("user.home"), ".waldap");
    }

    private static Handler addStatisticsHandler(Handler handler) {
        // The graceful shutdown is implemented via the statistics handler.
        // See the following: https://bugs.eclipse.org/bugs/show_bug.cgi?id=420142
        final StatisticsHandler statisticsHandler = new StatisticsHandler();
        statisticsHandler.setHandler(handler);
        return statisticsHandler;
    }
}
