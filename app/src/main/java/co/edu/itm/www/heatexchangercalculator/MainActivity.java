/*
 * Copyright 2017 Luis F. Cardona S.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.edu.itm.www.heatexchangercalculator;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemSelectedListener;

import java.text.NumberFormat;

import static co.edu.itm.www.heatexchangercalculator.R.mipmap.ic_launcher;
import static java.lang.Math.exp;
import static java.lang.Math.log;

public class MainActivity extends AppCompatActivity
{

    public String fluidoHot = "Agua";
    public String fluidoCold = "Agua";
    public String tipoIntercambiador = "concentricoParalelo";
    public String datoConocido = "tempSalidaHot";
    public double UpdateFlag =1;

    private double tempCinlet = 20;//°C
    private double tempHinlet = 160;//°C
    private double tempCOutlet = 80;//°C
    private double tempHOutlet = 114;//°C
    private double tempCpromedio = 50;//°C
    private double tempHpromedio = 137;//°C
    private double tempMax = tempHinlet;//°C
    private double tempMin = tempCinlet;//°C
    private double tempSpan = 0;

    private double mfhotflow = 2;//kg/s
    private double mfcoldflow = 1.2;//kg/s

    private double Cpcoldflow = 1;//J/(kg °C)
    private double Cphotflow = 1;//J/(kg °C)
    private double cHotflow = 1;//W/°C
    private double cColdflow = 1;//W/°C
    private double cmin = 1;//W/°C
    private double cmax = 1;//W/°C
    private double ratioc = 1;
    private double ratiocmin=0.05;

    private double efectividad = 0.5;
    private double NTU = 1;
    private double coeficienteU = 640;//W/(m²°C)
    private double QpuntoMax = 1;//W
    private double QpuntoReal = 1;//W
    private double Areasuperficial = 1;//m²
    private double datoEntrada = 114;//esto puede ser °C o m2 dependiendo del caso

    private TextView mfhotflowTextView;
    private TextView mfcoldflowTextView;
    private TextView tempcinletTextView;
    private TextView temphinletTextView;
    private TextView tempCOutletTextView;
    private TextView tempHOutletTextView;
    private TextView AreasuperficialTextView;
    private TextView AsTextView;
    private TextView NTUTextView;
    private TextView efectividadTextView;
    private TextView QpuntoRealTextView;
    private TextView cColdflowTextView;
    private TextView cHotflowTextView;
    private TextView ratiocTextView;
    private TextView coefUTextView;
    private TextView datoConocidoTextView;



    private static final NumberFormat numeroFormat = NumberFormat.getInstance();
    private static final NumberFormat tempFormat = NumberFormat.getInstance();
    private static final NumberFormat mfFormat = NumberFormat.getInstance();

    //Las temperaturas de salida no pueden ser menores que las de entrada, inferior y superior
    // Esta rutina es util cuando las temperaturas de entrada de ambos fluidos son iguales
    public void limitadorTemperaturasSalida()
    {
         if(tempHOutlet<tempCinlet)
         {
            tempHOutlet = tempCinlet;
         }
         else if(tempHOutlet>tempHinlet)
         {
             tempHOutlet = tempHinlet;
         }

         if(tempCOutlet<tempCinlet)
         {
             tempCOutlet = tempCinlet;
         }
         else if(tempCOutlet>tempHinlet)
         {
             tempCOutlet = tempHinlet;
         }
    }

    public void calculoEfectividad(){

        if(datoConocido=="areaSuperficial")
        {
            if (tipoIntercambiador == "concentricoParalelo")
            {
                efectividad = (1-exp(-NTU*(1+ratioc)))/(1+ratioc);

            }

            if (tipoIntercambiador == "concentricoContraflujo")
            {
                if (ratioc==1)
                {
                    efectividad = NTU/(1+NTU);
                }
                else
                {
                    efectividad = (1 - exp(-NTU * (1 - ratioc))) / (1 - ratioc * exp(-NTU * (1 - ratioc)));
                }
            }

            //se cumple en cualquier caso
            if (ratioc<ratiocmin)
            {
                efectividad = 1-exp(-NTU);
            }

        }
        else
        {
            if(tempCOutlet == tempHOutlet)
            {
                efectividad = 0;
            }
            else
            {
                efectividad = QpuntoReal / QpuntoMax;
            }
        }

        if (efectividad>1)
        {
            efectividad = 1;
        }

    }


    public void calculoNTU(){

        if(datoConocido=="areaSuperficial")
        {
            NTU = coeficienteU*Areasuperficial/cmin;
        }
        else
        {
            if (tipoIntercambiador == "concentricoParalelo")
            {
                NTU = -log(1 - efectividad * (1 + ratioc)) / (1 + ratioc);
            }

            if (tipoIntercambiador == "concentricoContraflujo")
            {
                if(ratioc==1)
                {
                    NTU = efectividad/(1-efectividad);
                }
                else
                {
                    NTU = (1 / (ratioc - 1)) * log((efectividad - 1) / (efectividad * ratioc - 1));
                }
            }

            if (efectividad == 0)
            {
                NTU = 0;

            }

            if (ratioc<ratiocmin)
            {
                NTU = -log(1-efectividad);
            }
        }

    }

//mejorar esta parte del codigo
    public void actualizaCp(){

        tempCpromedio = (tempCinlet + tempCOutlet)/2;
        tempHpromedio = (tempHinlet + tempHOutlet)/2;

        if(fluidoHot=="Agua")
        {
            //datos para agua
            Cphotflow = 0.0001*tempHpromedio*tempHpromedio*tempHpromedio-0.0197*tempHpromedio*tempHpromedio+1.3319*tempHpromedio+4169.3;
        }
        if(fluidoHot=="Aire")
        {
            //datos para aire
            Cphotflow = 0.0005*tempHpromedio*tempHpromedio-0.0204*tempHpromedio+1006.6;//J/(kg °C)
        }

        if(fluidoCold=="Agua")
        {
            //agua
            Cpcoldflow = 0.0001*tempCpromedio*tempCpromedio*tempCpromedio-0.0197*tempCpromedio*tempCpromedio+1.3319*tempCpromedio+4169.3;
        }
        if(fluidoCold=="Aire")
        {
            //aire
            Cpcoldflow = 0.0005*tempCpromedio*tempCpromedio-0.0204*tempCpromedio+1006.6;//J/(kg °C)
        }

    }

    public void actualizaCalculo() {

        //Ciclo que actualiza el Cp
        for(int i = 0; i < 1; i++)
        {
            actualizaCp();

            cHotflow = mfhotflow*Cphotflow;
            cColdflow = mfcoldflow*Cpcoldflow;

            if (cHotflow > cColdflow)
            {
                cmin = cColdflow;
                cmax = cHotflow;
            }
            else
            {
                cmin = cHotflow;
                cmax = cColdflow;
            }

            cColdflowTextView = (TextView) findViewById(R.id.textView14);
            cColdflowTextView.setText(numeroFormat.format(cColdflow/1000)+ " kW/°C");

            cHotflowTextView = (TextView) findViewById(R.id.textView17);
            cHotflowTextView.setText(numeroFormat.format(cHotflow/1000)+ " kW/°C");

            ratioc = cmin/cmax;
            ratiocTextView = (TextView) findViewById(R.id.textView21);
            ratiocTextView.setText(numeroFormat.format(ratioc));

            QpuntoMax = cmin*(tempHinlet - tempCinlet);

            if(datoConocido == "areaSuperficial")
            {
                calculoNTU();
                calculoEfectividad();
                QpuntoReal = efectividad*QpuntoMax;
                tempCOutlet = tempCinlet + QpuntoReal/cColdflow;
                tempHOutlet = tempHinlet - QpuntoReal/cHotflow;
            }
            else
            {
                if(datoConocido == "tempSalidaHot")
                {
                    QpuntoReal = cHotflow*(tempHinlet - tempHOutlet);
                    tempCOutlet = tempCinlet + QpuntoReal/cColdflow;
                }
                if(datoConocido == "tempSalidaCold")
                {
                    QpuntoReal = cColdflow*(tempCOutlet - tempCinlet);
                    tempHOutlet = tempHinlet - QpuntoReal/cHotflow;
                }
                calculoEfectividad();
                calculoNTU();
                Areasuperficial = (NTU * cmin) / coeficienteU;
            }
            tempHOutletTextView = (TextView) findViewById(R.id.tempHOutlet_TextView);
            tempHOutletTextView.setText(tempFormat.format(tempHOutlet) + "°C");
            tempCOutletTextView = (TextView) findViewById(R.id.tempCOutlet_TextView);
            tempCOutletTextView.setText(tempFormat.format(tempCOutlet) + "°C");
            QpuntoRealTextView = (TextView) findViewById(R.id.QpuntoReal_TextView);
            QpuntoRealTextView.setText(numeroFormat.format(QpuntoReal/1000) + " kW");
            efectividadTextView = (TextView) findViewById(R.id.efectividad_TextView);
            efectividadTextView.setText(numeroFormat.format(efectividad));
            NTUTextView = (TextView) findViewById(R.id.NTU_TextView);
            NTUTextView.setText(numeroFormat.format(NTU));
            AreasuperficialTextView = (TextView) findViewById(R.id.Areasuperficial_TextView);
            AreasuperficialTextView.setText(numeroFormat.format(Areasuperficial)+" m²");

            //Sección de rutina que garantiza que existe un delta entre las temperaturas de entrada y salida
            //cuando el intercambiador es concentrico
            //(actualiza limites de datoConocido)

            SeekBar datoConocidoSeekBar = (SeekBar) findViewById(R.id.datoConocido_SeekBar);
            if (tipoIntercambiador == "concentricoParalelo")
            {

                if (tempCOutlet>=tempHOutlet)
                {
                    if (datoConocido == "tempSalidaHot")
                    {
                        tempSpan = tempHOutlet+1;
                        tempMax = tempHinlet+1;
                        //Se restan las temperaturas de los extremos del rango posible (ambas dan valores NaN)
                        datoConocidoSeekBar.setMax((int)tempMax-(int)tempSpan-2);
                    }
                }

                if (tempHOutlet<=tempCOutlet)
                {
                    if (datoConocido == "tempSalidaCold")
                    {
                        tempSpan = tempCinlet;
                        tempMax =  tempCOutlet;
                        datoConocidoSeekBar.setMax((int)tempMax-(int)tempSpan-2);
                    }
                }
            }
            else
            {
                tempSpan = tempCinlet+1;
                tempMax = tempHinlet+1;
                //Se restan las temperaturas de los extremos del rango posible (ambas dan valores NaN)
                datoConocidoSeekBar.setMax((int)tempMax-(int)tempSpan-2);

            }

            if(UpdateFlag==1)
            {
                if(tempHinlet==tempCinlet)
                {
                    UpdateFlag=1;
                }
                else
                {
                    SeekBar thEntSeekBar = (SeekBar) findViewById(R.id.th_ent_seekbar);
                    datoConocidoSeekBar.setMax(thEntSeekBar.getProgress()-(int)tempSpan);
                    datoConocidoSeekBar.setProgress(thEntSeekBar.getProgress()-(int)tempSpan);
                    UpdateFlag=0;
                }
            }
        }//end for para actualizar Cp



    }//end of actualizaCalculo()


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        actualizaCalculo();

        //Spinner fluido1
        Spinner spinner1 = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Fluidos, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter);

        //Spinner fluido2
        Spinner spinner2 = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.Fluidos, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        //Spinner Tipo de intercambiador
        Spinner spinner3 = (Spinner) findViewById(R.id.spinner3);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.Tipo, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);

        //Spinner Dato conocido
        Spinner spinner4 = (Spinner) findViewById(R.id.spinner4);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this, R.array.DatoConocido, android.R.layout.simple_spinner_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner4.setAdapter(adapter4);

        tempFormat.setMinimumFractionDigits(0);
        tempFormat.setMaximumFractionDigits(0);

        mfFormat.setMinimumFractionDigits(2);
        mfFormat.setMaximumFractionDigits(2);

        coefUTextView = (TextView) findViewById(R.id.coefU_textView);
        SeekBar coefUseekBar = (SeekBar) findViewById(R.id.coefU_seekBar);
        coefUseekBar.setOnSeekBarChangeListener(seekBarListener5);

        tempcinletTextView = (TextView) findViewById(R.id.temp_c_inletTextView);
        SeekBar tcEntSeekBar = (SeekBar) findViewById(R.id.tc_ent_seekbar);
        tcEntSeekBar.setOnSeekBarChangeListener(seekBarListener1);

        temphinletTextView = (TextView) findViewById(R.id.temp_h_inletTextView);
        SeekBar thEntSeekBar = (SeekBar) findViewById(R.id.th_ent_seekbar);
        thEntSeekBar.setOnSeekBarChangeListener(seekBarListener2);

        mfcoldflowTextView = (TextView) findViewById(R.id.mf_coldflowTextView);
        SeekBar mfcoldSeekBar = (SeekBar) findViewById(R.id.mfcSeekBar);
        mfcoldSeekBar.setOnSeekBarChangeListener(seekBarListener3);

        mfhotflowTextView = (TextView) findViewById(R.id.mf_hotflowTextView);
        SeekBar mfhotSeekBar = (SeekBar) findViewById(R.id.mfhSeekBar);
        mfhotSeekBar.setOnSeekBarChangeListener(seekBarListener4);

        datoConocidoTextView = (TextView) findViewById(R.id.datoConocido_textView);
        final SeekBar datoConocidoSeekBar = (SeekBar) findViewById(R.id.datoConocido_SeekBar);
        datoConocidoSeekBar.setOnSeekBarChangeListener(seekBarListener6);

        ImageView img= (ImageView) findViewById(R.id.image);
        img.setImageResource(R.drawable.ic_concentricoparalelo);

        spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                if(position==0)//Agua
                {
                    fluidoHot="Agua";
                }

                if(position==1)//Aire
                {
                    fluidoHot="Aire";
                }
                //((TextView) adapterView.getChildAt(0)).setTextSize(18);
                actualizaCalculo();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio
            }

            });

        spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

                if(position==0)//Agua
                {
                    fluidoCold="Agua";
                 }

                if(position==1)//Aire
                {
                    fluidoCold="Aire";
                }
                //((TextView) adapterView.getChildAt(0)).setTextSize(18);
                actualizaCalculo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio
            }

        });

        spinner3.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                ImageView img= (ImageView) findViewById(R.id.image);
                if(position==0)//Concentrico paralelo
                {
                    img.setImageResource(R.drawable.ic_concentricoparalelo);
                    tipoIntercambiador = "concentricoParalelo";
                }

                if(position==1)//Concentrico contraflujo
                {
                    img.setImageResource(R.drawable.ic_concentricocontraflujo);
                    tipoIntercambiador = "concentricoContraflujo";
                }
                //((TextView) adapterView.getChildAt(0)).setTextSize(18);
                actualizaCalculo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio
            }
        });//cierra spinner3

        //Spinner de dato conocido
        spinner4.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                if(position==0)
                {
                    datoConocido = "tempSalidaHot";
                    tempHOutlet = datoEntrada;
                    datoConocidoTextView.setText(tempFormat.format(datoEntrada+tempSpan) + " °C");
                }

                if(position==1)
                {
                    datoConocido = "tempSalidaCold";
                    tempCOutlet = datoEntrada;
                    datoConocidoTextView.setText(tempFormat.format(datoEntrada+tempSpan) + " °C");
                }

                if(position==2)
                {
                    datoConocido = "areaSuperficial";
                    datoConocidoSeekBar.setMax(30);
                    Areasuperficial=datoEntrada;
                    datoConocidoTextView.setText(tempFormat.format(datoEntrada) + " m²");
                }
                //((TextView) adapterView.getChildAt(0)).setTextSize(18);
                actualizaCalculo();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio
            }
        });//cierra spinner4

    }//cierra oncreate


    //seekbar de temperatura fria
    private final OnSeekBarChangeListener seekBarListener1 =
            new OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Se suma 1 para evitar tempCinlet sea 0°C, lo cual para el agua sería hielo
                    //(recordar que progress=0 en la izquierda)
                    tempCinlet = progress+1;
                    tempMax = tempHinlet;
                    tempMin = tempCinlet;
                    tempSpan = tempCinlet;
                    tempcinletTextView.setText(tempFormat.format(tempCinlet) + "°C");
                    if (tempCinlet>=tempHinlet)
                    {
                            SeekBar thEntSeekBar2 = (SeekBar) findViewById(R.id.th_ent_seekbar);
                            thEntSeekBar2.setProgress((int)tempCinlet-1);
                            //se resta uno para que el progress de ambas barras sean iguales
                            // (recordar que se sumó 1 a tempCinlet)
                            if((datoConocido == "tempSalidaHot")||(datoConocido == "tempSalidaCold"))
                            {
                                SeekBar datoConocidoSeekBar = (SeekBar) findViewById(R.id.datoConocido_SeekBar);
                                datoConocidoSeekBar.setProgress((int) tempCinlet - 1);
                                datoEntrada = tempCinlet;
                                datoConocidoTextView.setText(tempFormat.format(datoEntrada) + " °C");
                                UpdateFlag=1;//igualdad de temperaturas
                            }
                    }
                    limitadorTemperaturasSalida();
                    actualizaCalculo();

                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            };


    private final OnSeekBarChangeListener seekBarListener2 =
            new OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //Se suma 1 para evitar tempCinlet sea 0°C, lo cual para el agua sería hielo
                    //(recordar que progress=0 en la izquierda)
                    tempHinlet = progress+1;
                    tempMax = tempHinlet;
                    tempMin = tempCinlet;
                    tempSpan = tempCinlet;
                    temphinletTextView.setText(tempFormat.format(tempHinlet) + "°C");
                    if (tempCinlet>=tempHinlet)
                    {
                        SeekBar tcEntSeekBar2 = (SeekBar) findViewById(R.id.tc_ent_seekbar);
                        tcEntSeekBar2.setProgress((int)tempHinlet-1);
                        if(!datoConocido.equals("areaSuperficial"))
                        {
                            SeekBar datoConocidoSeekBar = (SeekBar) findViewById(R.id.datoConocido_SeekBar);
                            datoConocidoSeekBar.setProgress((int) tempHinlet - 1);
                            datoEntrada = tempHinlet;
                            datoConocidoTextView.setText(tempFormat.format(datoEntrada) + " °C");
                            UpdateFlag=1;//igualdad de temperaturas
                        }
                    }
                    limitadorTemperaturasSalida();
                    actualizaCalculo();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            };


    private final OnSeekBarChangeListener seekBarListener3 =
            new OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mfcoldflow = 0.1+(float)progress/100;
                    mfcoldflowTextView.setText(mfFormat.format(mfcoldflow) + " kg/s");
                    actualizaCalculo();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            };

    private final OnSeekBarChangeListener seekBarListener4 =
            new OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mfhotflow = 0.1+(float)progress/100;
                    mfhotflowTextView.setText(mfFormat.format(mfhotflow) + " kg/s");
                    actualizaCalculo();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            };


    private final OnSeekBarChangeListener seekBarListener5 =
            new OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    coeficienteU = 10+progress;
                    coefUTextView.setText(tempFormat.format(coeficienteU) + " W/m²°C");
                    actualizaCalculo();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            };

    private final OnSeekBarChangeListener seekBarListener6 =
            new OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    datoEntrada = progress;
                    if(datoConocido == "tempSalidaHot")
                    {
                        tempHOutlet = datoEntrada + tempSpan;
                        datoConocidoTextView.setText(tempFormat.format(tempHOutlet) + " °C");
                    }
                    if(datoConocido == "tempSalidaCold")
                    {
                        tempCOutlet = datoEntrada + tempSpan;
                        datoConocidoTextView.setText(tempFormat.format(tempCOutlet) + " °C");
                    }
                    if(datoConocido == "areaSuperficial")
                    {
                        Areasuperficial=datoEntrada;
                        datoConocidoTextView.setText(tempFormat.format(Areasuperficial) + " m²");
                    }
                    actualizaCalculo();
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            };

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }
*/

/*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/



}



