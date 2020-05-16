package Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class Controller {
    @FXML
    TextArea chatArea=null;
    @FXML
    TextField textField;
    @FXML
    Button btn1;
     String Whom="To";
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    final String IP_ADRESS = "localhost";
    final int PORT = 8289;

    protected static final int LAST_LINES = 10;

    public boolean isAuthorized;
    @FXML HBox upperPanel;
    @FXML public HBox bottomPanel;
    @FXML TextField loginField;
    @FXML PasswordField passwordField;
    @FXML HBox sendPanel;
    @FXML Button but1;
    @FXML Button but2;
    @FXML Button but3;
    @FXML Button sendTo;
//     Button[] buttons=new Button[]{but1,but2,but3};

    public void setAuthorized(boolean isAuthorized){
        this.isAuthorized=isAuthorized;
        if(!isAuthorized){
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
                sendPanel.setVisible(false);
                sendPanel.setManaged(false);
        }
        else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
                sendPanel.setVisible(true);
                sendPanel.setManaged(true);
        }
    }

    public void toFile (String strFromServer)
    {
        try {
            FileWriter fileWriter = new FileWriter("file.txt", true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(strFromServer+"\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
public void showLast ()
    {
        try {
            FileReader fileReader = new FileReader("file.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            List<String> fromFile = new LinkedList<>();
            while ((line = bufferedReader.readLine()) != null) {
                fromFile.add(line + '\n');
            }
            bufferedReader.close();
            int startFrom;
            if (fromFile.size() >= LAST_LINES) startFrom = fromFile.size() - LAST_LINES;
            else startFrom = 0;
            for (int i = startFrom; i < fromFile.size();  i++) {
                if (fromFile.size() != 0)
                    chatArea.appendText(fromFile.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void connect() {
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            setAuthorized(false);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            String strFromServer = in.readUTF();
                            if (strFromServer.startsWith("/authok")) {
                                setAuthorized(true);
                                showLast();
                                break;
                            }
                            chatArea.appendText(strFromServer + "\n");
                        }
                        while (true) {
                            String strFromServer = in.readUTF();
                            if (strFromServer.equalsIgnoreCase("/end")) {
                                break;
                            }
                            chatArea.appendText(strFromServer);
                            chatArea.appendText("\n");
                            toFile(strFromServer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   public void sendTo(String sendTo)
   {
       try {
           out.writeUTF("To"+" "+sendTo+"  " +textField.getText());
           textField.clear();
           textField.requestFocus();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    public void sendMulty() {
        try {
            out.writeUTF("To"+Whom+textField.getText());
            textField.clear();
            textField.requestFocus();
            Whom="To";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void but1()
    { sendTo(but1.getText());
    }
    public void but2()
    { sendTo(but2.getText());
    }
    public void but3()
    { sendTo(but3.getText());
    }
    public void onAuthClick() {
        try {
            out.writeUTF( "/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void tryToAuth(ActionEvent actionEvent) {
        if(socket==null|| socket.isClosed()){
            connect();
        }
        try {
            out.writeUTF( "/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  public void Dispose() throws IOException {
      System.out.println("Отправляем сообщение на сервер о завершении работы");
      if( out !=null){
          out.writeUTF("/end");
      }
  }

}

