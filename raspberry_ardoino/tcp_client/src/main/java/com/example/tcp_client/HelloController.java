package com.example.tcp_client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    public Label label_temperature;
    public ProgressBar progressBar_temperature;
    public Label label_humidity;
    public ProgressBar progressBar_humidity;

    public Circle red_led;
    public Circle blue_led;

    public Button button_red_led;
    public Button button_blue_led;

    private boolean state_of_button_red_led;
    private boolean state_of_button_blue_led;

    private final Socket socket;

    public HelloController(){
        this.state_of_button_blue_led=false;
        this.state_of_button_red_led=false;
        this.socket=new Socket();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        try {
            this.socket.connect(new InetSocketAddress("192.168.0.9",9999));
            this.received_data_from_server(new ActionEvent());
        } catch (IOException e) {
            //throw new RuntimeException(e);
            System.out.printf("%s\r\n",e.getMessage());
            System.out.printf("%s\r\n","서버 통신 실패");
        }
    }

    private double change_progressBar_value(double x, double in_min, double in_max, double out_min, double out_max){
        return (x-in_min) * (out_max-out_min) / (in_max-in_min) + out_min;
    }


    private void received_data_from_server(ActionEvent actionEvent) {
        Thread thread_of_receiving = new Thread(()->{
            while(true){
                try {
                    byte[] bytes_data=new byte[512]; //buf siz
                    final InputStream input_stream= this.socket.getInputStream();// 소켓에서 데이터
                    final int read_byte_count=input_stream.read(bytes_data);
                    if(read_byte_count==-1){
                        throw new IOException();
                    }
                    final String[] parsing_data =
                            new String(bytes_data, 0, read_byte_count).trim().split(",");
                    //System.out.printf("%s %s", parsing_data[0],parsing_data[1]);
                    if(parsing_data.length !=2) continue;
                    final double temperature = Double.parseDouble(parsing_data[0]);
                    final double humidity = Double.parseDouble(parsing_data[1]);

                    System.out.printf("tem: %s\r\n", temperature);
                    System.out.printf("hum: %s\r\n", humidity);

                    final double changed_temperature_for_progressBar=
                            this.change_progressBar_value(temperature,0.0,40.0, 0.0,1.0);
                    final double changed_humidity_for_progressBar=
                            this.change_progressBar_value(humidity,0.0,100.0, 0.0,1.0);
                    Platform.runLater(()->{
                        progressBar_temperature.setProgress(changed_temperature_for_progressBar);
                        progressBar_humidity.setProgress(changed_humidity_for_progressBar);
                        label_temperature.setText(parsing_data[0]);
                        label_humidity.setText(parsing_data[1]);
                    });
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                    System.out.printf("%s\r\n", e.getMessage());
                    System.out.printf("%s\r\n", "입력 데이터 오류");
                    try {
                        this.socket.close();
                    } catch (IOException ex) {
                        //throw new RuntimeException(ex);
                        System.out.printf("%s\r\n", e.getMessage());
                        System.out.printf("%s\r\n", "socket.close() error");
                    }
                }
            }
        });
        thread_of_receiving.start();

    }

    public void buttonOnClickedRedLED(ActionEvent event){
        System.out.printf("%s\r\n", "red led button click");
        if(this.socket.isConnected()){
            this.state_of_button_red_led ^=true;//xor
            if(this.state_of_button_red_led){
                this.red_led.setVisible(true);
                this.red_led.setFill(Paint.valueOf("red"));

                try {
                    byte[] byte_data=("RED_LED_ON\n").getBytes("UTF-8");
                    final var output_stream = this.socket.getOutputStream();
                    output_stream.write(byte_data);
                    output_stream.flush();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.button_red_led.setText("RED LED ON");
            }else{
                this.red_led.setFill(Paint.valueOf("black"));
                try {
                    byte[] byte_data=("RED_LED_OFF\n").getBytes("UTF-8");
                    final OutputStream outputStream=this.socket.getOutputStream();
                    outputStream.write(byte_data);
                    outputStream.flush();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.button_red_led.setText("RED LED OFF");
            }

        }
    }
    public void buttonOnClickedBlueLED(ActionEvent event){
        System.out.printf("%s\r\n", "blue led button click");
        if(this.socket.isConnected()){
            this.state_of_button_blue_led ^=true;//xor
            if(this.state_of_button_blue_led){
                this.blue_led.setVisible(true);
                this.blue_led.setFill(Paint.valueOf("blue"));

                try {
                    byte[] byte_data=("BLUE_LED_ON\n").getBytes("UTF-8");
                    final var output_stream = this.socket.getOutputStream();
                    output_stream.write(byte_data);
                    output_stream.flush();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.button_blue_led.setText("BLUE LED ON");
            }else{
                this.blue_led.setFill(Paint.valueOf("black"));
                try {
                    byte[] byte_data=("BLUE_LED_OFF\n").getBytes("UTF-8");
                    final OutputStream outputStream=this.socket.getOutputStream();
                    outputStream.write(byte_data);
                    outputStream.flush();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                this.button_blue_led.setText("BLUE LED OFF");
            }

        }
    }
}