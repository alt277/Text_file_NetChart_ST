package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {
    public static final int TIMEOUT=120*1000;
//    Controller controller;
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    private String adress;
//    public static List<String> reciever;
//    public List<String> getReciever() {
//        return reciever;
//    }

    public String getName() {
        return name;
    }
    public ClientHandler(MyServer myServer, Socket socket) {
        try {
            this .myServer = myServer;
            this .socket = socket;
            this .in = new DataInputStream(socket.getInputStream());
            this .out = new DataOutputStream(socket.getOutputStream());
//            this .name = "" ;

            new Thread(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException( "Проблемы при создании обработчика клиента" );
        }
    }
    public void authentication() throws IOException {

        while (true) {
            Timer timeoutTimer = new Timer(true);
            timeoutTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            if (name == null) {
                                System.out.println("authentication is terminated caused by timeout expired");
                                sendMsg("Истекло время ожидания подключения!");
                                Thread.sleep(100);
                                socket.close();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, TIMEOUT);


            String str = in.readUTF();
            synchronized (this) {
                if (str.startsWith("/auth")) {
                    String[] parts = str.split("\\s");
                    String nick =
                            myServer.getAuthService().getNickByLoginPass(parts[1], parts[2]);
                    if (nick != null) {
                        if (!myServer.isNickBusy(nick)) {
                            sendMsg("/authok " + nick);
                            name = nick;
//                            Stage stage = (Stage) controller.bottomPanel.getScene().getWindow();
//                            stage.setTitle(nick);
                            myServer.broadcastMsg(name + " зашел в чат");
                            myServer.subscribe(this);
                            return;
                        } else {
                            sendMsg("Учетная запись уже используется");
                        }
                    } else {
                        sendMsg("Неверные логин/пароль");
                    }
                }
            }
        }
    }
    public void readMessages() throws IOException {

        while ( true ) {
            String strFromClient = in.readUTF();
            if(strFromClient.equals( "/end" )) {
                return ;
            }
            if (strFromClient.startsWith( "/to" )) {
                String[] parts = strFromClient.split( "\\s" );
                 adress = parts[1];
                 myServer.singleMsg("oт: "+name+""+strFromClient,adress);
            }
            else if (strFromClient.startsWith( "To" )) {
                String[] parts = strFromClient.split("\\s");
                for(int i=2;i<parts.length;i++)
                { strFromClient="";
                    strFromClient+=parts[i]; }
                        myServer.singleMsg("от: "+name+" "+ strFromClient,parts[1]);

            }
           else  {
                myServer.broadcastMsg("от: "+name + ": " + strFromClient);
           System.out.println( "от " + name + ": " + strFromClient);}



        }
    }
    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() {
        myServer.unsubscribe( this );
        myServer.broadcastMsg(name + " вышел из чата" );
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}