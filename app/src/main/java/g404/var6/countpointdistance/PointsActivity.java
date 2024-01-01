package g404.var6.countpointdistance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.text.DecimalFormat;

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONException;
import org.json.JSONObject;


public class PointsActivity extends AppCompatActivity {

    //объявление объектов для взаимодействия с ними через код
    private TextView result;
    private EditText x1, y1, z1, x2, y2, z2;
    private Button btnGetSolve;

    Runnable GetSolve; //будет выполняться в отдельном потоке
    private String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //поиск и присвоение объектам в коде соответствующих виджетов в макете activity_main.xml
        result =  findViewById(R.id.result);
        x1 =  findViewById(R.id.x1);
        y1 = findViewById(R.id.y1);
        z1 =  findViewById(R.id.z1);
        x2 =  findViewById(R.id.x2);
        y2 = findViewById(R.id.y2);
        z2 =  findViewById(R.id.z2);
        btnGetSolve = (Button) findViewById(R.id.button_Calculate);

        btnGetSolve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //строковым переменным присваиваются значения коэффициентов
                //квадратного уравнения, взятые из введенных в виджеты
                //редактирования текста
                String px1 = x1.getText().toString();
                String py1 = y1.getText().toString();
                String pz1 = z1.getText().toString();

                String px2 = x2.getText().toString();
                String py2 = y2.getText().toString();
                String pz2 = z2.getText().toString();
                //построение GET-запроса
                URL =getString(R.string.URL)+"?x1=" + px1 + "&y1=" + py1 + "&z1="+pz1+"&x2=" + px2 + "&y2=" + py2 + "&z2="+pz2;
                //создание нового потока для функции GetSolve()
                Thread Process = new Thread(GetSolve);
                //запуск потока на исполнение
                Process.start();
            }
        });

        GetSolve = new Runnable() {
            String request = "";
            @Override
            public void run() {
                //создание нового HTTPS-соединения
                HttpsURLConnection connection = null;
                //создание строкового буфера для приема ответа от сервера
                StringBuffer buffer = new StringBuffer();
                try {
                    //создание новой ссылки с текстом GET-запроса
                    URL url = new URL(URL);
                    //открытие соединения
                    connection = (HttpsURLConnection) url.openConnection();
                    connection.connect();
                    //запись входного потока в stream
                    InputStream stream = connection.getInputStream();
                    //поток читается по строкам, создаем строку line
                    String line = "";
                    //инициализируем строковый ответ
                    request = "";
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                    //построчно через читателя входного потока reader пишем в request ответ
                    while ((line = reader.readLine()) != null) {
                        request += line;
                    }
                    stream.close();
                    reader.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    request = "";
                } finally {
                    //не забываем закрыть соединение после HTTPS-запроса
                    connection.disconnect();
                    //метод, который выполняется в главном потоке (UI-потоке)
                    runOnUiThread(new Runnable() {
                        @Override
                        //переопределение метода run для парсинга ответа
                        //и вывода его в виджет в главном потоке
                        public void run() {
                            try {
                                JSONObject jObject = new JSONObject(request);
                                result.setText(jObject.getString("mes"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }

            }
        };

    }

    //сохранинение значений, которые могут потеряться в ходе смены ориентации
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("distance", result.getText().toString());
        super.onSaveInstanceState(outState);
    }

    //восстановление потерявшихся значений
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        result.setText(savedInstanceState.getString("distance"));
        super.onRestoreInstanceState(savedInstanceState);
    }
}

