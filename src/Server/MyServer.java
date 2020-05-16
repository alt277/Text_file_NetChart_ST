package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class MyServer {
    private final int PORT = 8288 ;
    private List<ClientHandler> clients;
    private AuthService authService;

    public AuthService getAuthService() {
        return authService;
    }
    public MyServer() {
        try (ServerSocket server = new ServerSocket(PORT)) {
            authService = new BaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while ( true ) {
                System.out.println( "Сервер ожидает подключения" );
                Socket socket = server.accept();
                System.out.println( "Клиент подключился" );
                new ClientHandler( this , socket);
            }
        } catch (IOException e) {
            System.out.println( "Ошибка в работе сервера" );
           e.printStackTrace();
        } finally {
            if (authService != null ) {
                authService.stop();
            }
        }
    }
    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                return true ;
            }
        }
        return false ;
    }
    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }
    public synchronized void singleMsg(String msg,String sendTo) {
        for (ClientHandler o : clients) {
            if(o.getName().equals(sendTo))
            {o.sendMsg(msg);
                System.out.println(" выполнилось условие для "+o.getName());
            }
            else{
                System.out.println("не выполнилось условие для "+o.getName());
            }
        }
    }
//    public synchronized void manyMSg(String msg) {
//        for (ClientHandler o : clients) {
//            for(String s: Server.ClientHandler.reciever)
//             if(o.getName().equals(s))
//             { o.sendMsg(msg);}
//        }
//    }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
    }
    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);

    }
}
