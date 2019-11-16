import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.image.ColorModel;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
//import java.util.prefs.Preferences;
//import java.util.Timer;
//import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.io.*;


public class SolarGUI extends JFrame implements ActionListener, ItemListener, WindowListener{
    //Deklarationen
    
    //Analyse Bild
    private static BufferedImage image;
    private static Graphics2D g2d;
    private static int i_height;
    private static int i_width; 
    private static double d_height;
    private static double d_width;
    
    //Vorschau Bild
    private static double d_scale_factor_width;
    private static double d_scale_factor_height;
    private static double d_scale_factor_definitiv;
    private static int i_preview_x;
    private static int i_preview_y;
    private static boolean bol_picture_loadet;
    
    //Analyse
    //private static boolean bol_found_ordinate = false;
    private static boolean bol_found_ursprung;
    private static boolean bol_found_abszisse;
    private static boolean bol_draw_coordinate_system;
    private static boolean bol_draw_scanpoint;
    private static int i_ordinate_x;
    private static int i_ordinate_y;
    private static int i_ursprung_x;
    private static int i_ursprung_y;
    private static int i_ursprung_x_offset;
    private static int i_ursprung_y_offset;
    private static int i_abszisse_x;
    private static int i_abszisse_y;
    private static int i_ordinate_laenge;
    private static int i_ordinate_breite;
    private static int i_ordinate_innenkante_x;
    private static int i_abszisse_laenge;
    private static int i_abszisse_breite;
    private static int i_abszisse_innenkante_y;
    private static int i_ordinate_offset;
    private static int i_ursprung_offset;
    private static int i;
    private static int j;
    //math
    private static int m;
    private static int r,s;
    private static int x_delta;
    private static boolean bol_y = false;
    private static boolean bol_draw;
    private static int[] arr_f_values_x_gui;
    private static int[] arr_f_values_y_gui;
    private static int[] arr_f_values_x_lagrange;
    private static int[] arr_f_values_y_lagrange;
    private static int i_support_point = 20;
    private static boolean bol_support_points_ok;
    private static String[] arr_lg_coeff;
    private static String[] arr_lg_coeff_part;
    private static String str_lg_polynom;
    private static boolean bol_evaluate;
    private static boolean bol_first_round;
    //draw polynom
    private static boolean bol_draw_polynom;
    private static int i_x_resolution = 100;
    private static int i_x_intervall;
    private static int[] arr_i_x_draw_polynom;
    private static int[] arr_i_y_draw_polynom;
    private static String str_lg_draw_polynom;
    //Farbe für Analyse
    private static Color color_read;
    private static int i_color;
    private static int i_color_black = -16777216;
    private static int i_color_white = -1;
    private static int i_color_search_counter;
    private static int i_color_pixelvalue;
    
    //File Chooser für das Öffnen eines Bildes
    private static JFileChooser chooser_open_picture;
    private static FileNameExtensionFilter filter;
    private static int i_return_value;
    
    //enum für states
    private static enum State{FAIL, INIT, RESET, ANALYZE, OPEN_IMAGE, WAIT};
    private static State state;
    private static State old_state;
    private static String str_fail;
    
    //GUI
    //Buttons
    private static JButton btn_open_picture;
    private static JButton btn_analyze_picture;
    //Labels
    private static JLabel lbl_bild_einfuegen;
    private static JLabel lbl_polynomgrad;
    private static JLabel lbl_abszisse;
    private static JLabel lbl_ordinate;
    private static JLabel lbl_ursprung;
    private static JLabel lbl_kurve_erfasst;
    private static JLabel lbl_interpolation_erfolgreich;
    //Textfields
    private static JTextField tf_polynomgrad;   //Textfeld für die Angabe des Polynomgrades
       
    /**
     * Konstruktor
     */
    public SolarGUI(){
        //Layout
        setTitle("Curve Approximizer \u00a9 Nico Korn");
        setSize(720,560);
        setLayout(null);
        getContentPane().setBackground(new Color(255,251,242)); //setBackground(Color.LIGHT_GRAY);
        setLocation(100,100);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        //open Picture Button
        btn_open_picture = new JButton("Bild Öffnen");
        btn_open_picture.setBounds(240,10,235,25);
        btn_open_picture.addActionListener(this);
        add(btn_open_picture);
        
        //analyze Picture Button
        btn_analyze_picture = new JButton("Bild Analysieren");
        btn_analyze_picture.setBounds(12,415,235,25);
        btn_analyze_picture.setEnabled(false);
        btn_analyze_picture.addActionListener(this);
        add(btn_analyze_picture);
            
        //label in Fenstermitte mit Anweisung
        lbl_bild_einfuegen = new JLabel("Um ein Diagramm zur Analyse einzufügen, klicken Sie auf Bild Öffnen");
        lbl_bild_einfuegen.setBounds(170,210,500,25);
        //add(lbl_bild_einfuegen);
        
        //label unter Analyse Button um den Polynomgrad festzulegen
        lbl_polynomgrad = new JLabel("Interpolationsgrad: ");
        lbl_polynomgrad.setBounds(12,450,500,25);
        add(lbl_polynomgrad);
        
        //textfield neben dem Label für Polynomgrad
        tf_polynomgrad = new JTextField();
        tf_polynomgrad.setBounds(148,450,100,25);
        tf_polynomgrad.setHorizontalAlignment(JTextField.RIGHT);
        add(tf_polynomgrad);
        
        //Status anzeige
        lbl_abszisse = new JLabel("Abszisse erfasst:   \u00D7");
        lbl_abszisse.setBounds(480,410,200,20);
        lbl_abszisse.setHorizontalAlignment(JLabel.RIGHT);
        add(lbl_abszisse);
        
        lbl_ordinate = new JLabel("Ordinate erfasst:   \u00D7");
        lbl_ordinate.setBounds(480,430,200,20);
        lbl_ordinate.setHorizontalAlignment(JLabel.RIGHT);
        add(lbl_ordinate);
        
        lbl_ursprung = new JLabel("Ursprung erfasst:   \u00D7");
        lbl_ursprung.setBounds(480,450,200,20);
        lbl_ursprung.setHorizontalAlignment(JLabel.RIGHT);
        add(lbl_ursprung);        
        
        lbl_kurve_erfasst = new JLabel("Kurve erfasst:   \u00D7");
        lbl_kurve_erfasst.setBounds(480,470,200,20);
        lbl_kurve_erfasst.setHorizontalAlignment(JLabel.RIGHT);
        add(lbl_kurve_erfasst);        
        
        lbl_interpolation_erfolgreich = new JLabel("Interpolation erfolgreich:   \u00D7");
        lbl_interpolation_erfolgreich.setBounds(480,490,200,20);
        lbl_interpolation_erfolgreich.setHorizontalAlignment(JLabel.RIGHT);
        add(lbl_interpolation_erfolgreich);        
        
        state = State.INIT;
        //old_state = null;
        
        addWindowListener(this);
        setVisible(true);
        
        //start FSM with main
        main(null);
    }
    
    public void main(String [ ] args){
        run();
    }
    
    public void run(){
        while(true){
            //System.out.println(state+"   "+old_state);
            //if(state != old_state){
                //System.out.println("detected state: "+state);
                switch(state){
                    case INIT:          init();
                                        state = State.WAIT;
                                        break;
                
                    case FAIL:          System.out.println("FAILLLLL");
                                        fail(str_fail);
                                        state = State.RESET;
                    break;
                
                    case RESET:         reset();
                                        state = State.WAIT;
                    break;
                               
                    case OPEN_IMAGE:    reset();
                                        open_picture();
                                        repaint();
                                        state = State.WAIT;
                    break;
                
                    case ANALYZE:       //System.out.println("analysiere Ordinate");
                                        bol_draw_scanpoint = true; //lasse im Diagramm Scanbalken erscheinen.... in Zusammenhang mit paint und der Methode analyze_ordinate
                                        analyze_ordinate(); //suche im Diagramm nach der Ordinate
                                        //System.out.println("Ordinate analysiert");
                                        if(state == State.FAIL){
                                            state = State.RESET;
                                            lbl_ordinate.setText("Ordinate erfasst:   \u00D7");
                                            break;
                                        }else{
                                            lbl_ordinate.setText("Ordinate erfasst:   \u2713");
                                        }
                                        //System.out.println("analysiere Abszisse");
                                        analyze_abszisse();  //suche im Diagramm nach der Abszisse
                                        //System.out.println("Abszisse analysiert");
                                        if(state == State.FAIL){
                                            state = State.RESET;
                                            lbl_abszisse.setText("Abszisse erfasst:   \u00D7");
                                            lbl_ursprung.setText("Ursprung erfasst:   \u00D7");
                                            break;
                                        }else{
                                            lbl_abszisse.setText("Abszisse erfasst:   \u2713");
                                            lbl_ursprung.setText("Ursprung erfasst:   \u2713");
                                        }
                                        bol_draw_scanpoint = false; //Scanbalken nicht mehr zeichnen.... in Zusammenhang mit paint und der Methode analyze_ordinate
                                        //System.out.println("beginne mit Lagrange Approximation");
                                        math_function(); //suche nach Funktion und bestimme Stützwerte
                                        if(state == State.FAIL){
                                            state = State.RESET;
                                            lbl_kurve_erfasst.setText("Kurve erfasst:   \u00D7");
                                            break;
                                        }else{
                                            lbl_kurve_erfasst.setText("Kurve erfasst:   \u2713");
                                        }
                                        //System.out.println("Lagrange Approximation beendet");
                                        draw_polynom(); //zeichne die Lagrange Interpolation
                                        if(state == State.FAIL){
                                            state = State.RESET;
                                            lbl_interpolation_erfolgreich.setText("Interpolation erfolgreich:   \u007D");
                                            break;
                                        }else{
                                            lbl_interpolation_erfolgreich.setText("Interpolation erfolgreich:   \u2713");
                                        }
                                        //repaint();
                                        state = State.RESET;
                                        //System.out.println("state im case analyze: "+state);
                    break;
                
                    case WAIT:          if(bol_picture_loadet && tf_polynomgrad.getText().length()!=0){
                                            btn_analyze_picture.setEnabled(true);
                                        }else{
                                            btn_analyze_picture.setEnabled(false);
                                        }
                                        state = State.WAIT;
                    
                    break;
                
                    default:            state = State.WAIT;
                
                }
                //old_state = state;
            //}
            try {
                Thread.sleep(10);
            }catch(InterruptedException ie) {
            }
        }
    }
    
     /**
     * init state
     */
    public void init(){
        btn_analyze_picture.setEnabled(false);
    }
    
     /**
     * init state
     */
    public void reset(){
        i_ordinate_x = 0;
        i_ordinate_y = 0;
        i_ursprung_x = 0;
        i_ursprung_y_offset = 0;
        i_ursprung_x_offset = 0;
        i_ursprung_y = 0;
        i_abszisse_x = 0;
        i_abszisse_y = 0;
        i_preview_x = 0;
        i_preview_y = 0;
        i_ordinate_laenge = 0;
        i_ordinate_breite = 0;
        i_abszisse_laenge = 0;
        i_abszisse_breite = 0;
        i_ordinate_offset = 0;
        i_ursprung_offset = 0;
        i_ordinate_innenkante_x = 0;
        i_abszisse_innenkante_y = 0;
        i_color_search_counter = 0;
        i = 0;
        j = 0;
        
        arr_f_values_x_gui = null;
        arr_f_values_y_gui = null;
        arr_f_values_x_lagrange = null;
        arr_f_values_y_lagrange = null;
        arr_lg_coeff = null;
        arr_lg_coeff_part = null;
        arr_i_x_draw_polynom = null;
        arr_i_y_draw_polynom = null;
        str_lg_polynom = null;
        
        bol_found_ursprung = false;
        bol_found_abszisse = false;
        bol_draw_coordinate_system = false;
        bol_draw = false;
        bol_draw_polynom = false;
        bol_y = false;
        bol_draw_scanpoint = false;
    }
    
     /**
     * init fail
     */
    public void fail(String str_fail){
        JOptionPane.showMessageDialog(this,
        str_fail,
        "Error",
        JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * analyze ordinate state
     * Diese verschachtelte for Schlaufe wird bis zum Schluss durchlaufen, auch nach dem der Ursprung erfasst werden konnte.
     */
    public void analyze_ordinate(){
        bol_found_ursprung = false;
        try{
            //wechsle zeile
            for(i = i_height-1; i > 0; i--){
                //repaint Aufruf damit der Scanbalken/Scanpunkt bei jedem Durchlauf aktualisiert wird
                repaint();
                waitProcessing(1);
                //wechsle Spalte
                for(j = i_width-1; j > 0; j--){
                    //Suche nach schwarzem Pixel
                    if(image.getRGB(j,i) == i_color_black && bol_found_ursprung != true){
                        //Koordinate des unteren Ende der Ordinate
                        i_ordinate_x = j;
                        i_ordinate_y = i;
                        //Zähle nach unten durch bis Ende Ordinate
                        while(bol_found_ursprung != true){
                            i--;
                            if(color_black_check(j, i) && color_black_check(j+2, i) && color_black_check(j+4, i) && color_black_check(j+8, i) && color_black_check(j+16, i) && color_black_check(j+32, i)){
                                //Koordinate des Ursprungs
                                i_ursprung_x = j;
                                i_ursprung_y = i;
                                //Länge der Ordinate
                                i_ordinate_laenge =  i_ordinate_y - i_ursprung_y;
                                bol_found_ursprung = true;
                                //System.out.println("X-Koordinate Ursprung: "+i_ursprung_x+", Y-Koordinate Ursprung: "+i_ursprung_y+", Länge der Ordinate: "+i_ordinate_laenge);
                            }
                        }
                    }
                    //Pixel counter erhöhen
                    i_color_search_counter++;
                }
            }
            i_ursprung_x_offset = i_ursprung_x;
            i_ursprung_y_offset = i_ursprung_y;
        }catch(ArrayIndexOutOfBoundsException e) {
           System.out.println("Ursprung kann nicht erfasst werden");
           str_fail = "Ursprung kann nicht erfasst werden";
           state = State.FAIL;
           System.out.println(state);
        }
    }
    
    /**
     * analyze abszisse state
     */
    public void analyze_abszisse(){
        j = i_ursprung_x;
        i = i_ursprung_y;
        bol_found_abszisse = false;
        bol_draw_coordinate_system = false;
        try{
            while(bol_found_abszisse != true){
                if(image.getRGB(j,i) == i_color_white){
                    i_abszisse_x = j; // - 2 um das weisse Pixel und start  abzuzählen
                    i_abszisse_y = i;
                    i_abszisse_laenge = i_abszisse_x - i_ursprung_x;
                    bol_found_abszisse = true;
                    bol_draw_coordinate_system = true;
                    //System.out.println("X-Koordinate Abszisse: "+i_abszisse_x+", Y-Koordinate Abszisse: "+i_abszisse_y+", Länge der Abszisse: "+i_abszisse_laenge);
                }
                j++;
            }
        }catch(ArrayIndexOutOfBoundsException e) {
            bol_found_abszisse = true;
            str_fail = "Abszisse";
            state = State.FAIL;
        }
    }
    
    /**
     * open picture
     */
    public void open_picture(){
        bol_picture_loadet = false;
        chooser_open_picture = new JFileChooser();
        filter = new FileNameExtensionFilter("JPG & GIF Images", "jpg", "gif");
        chooser_open_picture.setFileFilter(filter);
        i_return_value = chooser_open_picture.showOpenDialog(this);
        if(i_return_value == JFileChooser.APPROVE_OPTION){
            //Bild laden
            try {
                image = ImageIO.read(new File(chooser_open_picture.getSelectedFile().getAbsolutePath()));   //Bild laden
                i_width = image.getWidth();                                                                 //Anzahl Reihen
                i_height = image.getHeight();                                                               //Anzahl Zeilen
                i_color_pixelvalue = i_height*i_width;                                                      //Anzahl Pixel
                bol_picture_loadet = true;
                repaint();
            }
            catch (IOException s) {
                s.printStackTrace();
                str_fail = "Konnte das Bild nicht laden";
                state = State.FAIL;
            }
        }
    }
    
    /**
     * make math function
     * In dieser Methode werden Stützwerte an der gegebenen Kurve erfasst und mittels Lagrange interpoliert
     */
    public void math_function(){
        m  = -1;  
        i_support_point = Integer.parseInt(tf_polynomgrad.getText()) + 1;
        x_delta = i_abszisse_laenge / i_support_point;      //x intervall aufgrund der mengen an Stützwerten
        arr_f_values_x_gui = new int[i_support_point];      //Array für x Werte, welche im gui angezeigt werden
        arr_f_values_y_gui = new int[i_support_point];      //Array für y Werte, welche im gui angezeigt werden
        arr_f_values_x_lagrange = new int[i_support_point]; //Array für x Werte, welche für das Lagrange Polynom gebraucht werden
        arr_f_values_y_lagrange = new int[i_support_point]; //Array für y Werte, welche für das Lagrange Polynom gebraucht werden
        arr_lg_coeff = new String[i_support_point];         //Array für die Lagrange Koeffizienten l
        arr_lg_coeff_part = new String[i_support_point];    //Array für die Lagrange Teilkoeffizienten
        bol_evaluate = true;
        i_abszisse_breite = 0;
        
        try{
            //x values
            for(i = 0; i<i_support_point; i++){
                m += 2;
                arr_f_values_x_gui[i] = i_ursprung_x + m*(x_delta / 2);
                arr_f_values_x_lagrange[i] = m*(x_delta / 2);
                r = i;
            }
        
            //Abszissenbreite / -höhe  bestimmen und als Offset benutzen
            i = i_ursprung_y;
            while(bol_y == false){    //suche Innenkante der Abszisse
                if(color_black_check(arr_f_values_x_gui[0],i)){
                    i_abszisse_breite ++;
                    i --;
                }else{
                    bol_y = true;
                }
            }
            System.out.println("Abszissenbreite: "+i_abszisse_breite);
        
            //y values
            for(i = 0; i<i_support_point; i++){
                bol_y = false;
                arr_f_values_y_gui[i] = i_ursprung_y - i_abszisse_breite;
                arr_f_values_y_lagrange[i] = 0;
                while(bol_y == false){
                    if(color_black_check(arr_f_values_x_gui[i],arr_f_values_y_gui[i])){
                        bol_y = true;
                    }else{
                        arr_f_values_y_gui[i]--;
                        arr_f_values_y_lagrange[i]++;
                    }
                    s = i;
                }
            }
            //draw x,y values into the gui diagramm (see paint function)
            System.out.println("Gefundene Stützwerte. x: "+r+"/"+(i_support_point-1)+", y: "+s+"/"+(i_support_point-1));
            bol_draw = true;
        }catch(ArrayIndexOutOfBoundsException e) {
                str_fail = "Stützwerte der Kurve wurden nicht alle gefunden. x: "+r+"/"+(i_support_point-1)+", y: "+s+"/"+(i_support_point-1);
                state = State.FAIL;
        }
        
        //mache lagrange interpolation
        try{
            for(i = 0; i<i_support_point; i++){            
                for(int o = 0; o<i_support_point; o++){
                    bol_evaluate = false;

                    if(arr_f_values_x_lagrange[o] != arr_f_values_x_lagrange[i]){     // according to lagrange rule: o != i 
                        bol_evaluate = true;
                    }           
                
                    if(bol_evaluate){
                        arr_lg_coeff_part[o] = "((x-"+arr_f_values_x_lagrange[o]+")/("+arr_f_values_x_lagrange[i]+"-"+arr_f_values_x_lagrange[o]+"))";
                        if(arr_lg_coeff[i] == null){    //null*(()/())*(()/()) verhindern
                            arr_lg_coeff[i] = arr_f_values_y_lagrange[i]+"*"+arr_lg_coeff_part[o];
                        }else{
                            arr_lg_coeff[i] = arr_lg_coeff[i]+"*"+arr_lg_coeff_part[o];
                        }
                    }
                }
            }
            
            for(i = 0; i<i_support_point; i++){
                //Lagrange Polynom zusammensetzen
                if(str_lg_polynom == null){
                    str_lg_polynom = arr_lg_coeff[i];
                }else{
                    str_lg_polynom = str_lg_polynom+"+"+arr_lg_coeff[i];
                }    
            }
        }catch(ArrayIndexOutOfBoundsException e) {
                str_fail = "Lagrange Interpolation/Approximation ist Fehlgeschlagen";
                state = State.FAIL;
        }
    }
    
    /**
     * Zeichnet das Polynom in Punkten
     */
    public void draw_polynom(){       
        bol_draw_polynom = false;
        ScriptEngine e = new ScriptEngineManager().getEngineByName("js");   //Script manager für das Parsen von einem mathematischen Stringausdruch in einen Integer Susdruck
        i_x_intervall = i_abszisse_laenge / i_x_resolution;
        arr_i_x_draw_polynom = new int[i_x_resolution];
        arr_i_y_draw_polynom = new int[i_x_resolution];
        //berechne die punkte in einer Schlaufe
        try{
            for(i=0; i<i_x_resolution-1; i++){
                arr_i_x_draw_polynom[i] = i_ursprung_x + i*i_x_intervall;
                str_lg_draw_polynom = str_lg_polynom.replace("x", Integer.toString(arr_i_x_draw_polynom[i]-i_ursprung_x));
                arr_i_y_draw_polynom[i] = i_ursprung_y - ((Number)e.eval(str_lg_draw_polynom)).intValue();
            }
            bol_draw_polynom = true;
            repaint();
            //while(bol_draw_polynom){
            //   //warte hier bis in der Paint Methoder der Boolean bol_draw_polynom auf false gesetzt wird
            //}
        }catch(ScriptException s){
            str_fail = "Funktion wurde Interpoliert, aber konnte nicht gezeichnet werden.";
            state = State.FAIL;
        }
    }
    
    /**
     * Color check
     */
    public boolean color_black_check(int i_x, int i_y){
        i_color = image.getRGB(i_x,i_y);    //lese Farbe an x,y aus
        for(int w = 0; w<500000; w++){ //standard 400000
            if(i_color_black + w == i_color){
                return true;
            }
        }
        return false;
    }

    
    /**
     * scaler for x coordinates
     */
    public int x_scale(int x_scale){
        return i_preview_x + (int)(d_scale_factor_definitiv*x_scale);
    }
    
    /**
     * scaler for y coordinates
     */
    public int y_scale(int y_scale){
        return i_preview_y + (int)(d_scale_factor_definitiv*y_scale);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g2d = (Graphics2D) g;
        
        //zeichne Bild
        if(image != null) {
            //Bild Skalierung
            d_height = (double)i_height;
            d_width = (double)i_width;
            d_scale_factor_width = 690 / d_width; 
            d_scale_factor_height = 360 / d_height; 
            //Zeichnen des Bildes im Vorschaufenster
            if((int)(d_height*d_scale_factor_width) > 360){
                i_preview_x = 15+345-((int)(d_width*d_scale_factor_height)/2);
                i_preview_y = 70;
                g.drawImage(image, i_preview_x, i_preview_y, (int)(d_width*d_scale_factor_height), (int)(d_height*d_scale_factor_height), this);
                d_scale_factor_definitiv = d_scale_factor_height;
            }else{
                i_preview_x = 15;
                i_preview_y = 70+180-((int)(d_height*d_scale_factor_width)/2);
                g.drawImage(image, i_preview_x, i_preview_y , (int)(d_width*d_scale_factor_width), (int)(d_height*d_scale_factor_width), this);
                d_scale_factor_definitiv = d_scale_factor_width;
            }
        }     
        
        //zeichne Feldrahmen
        g2d.drawRect(15,70,690,360);

        //zeichne Ordinate
        //g2d.setStroke(new BasicStroke(6));
        //zeichne erkanntes Koordinatensystem (nur die wichtigsten Elemente beim Ursprung)
        //if(bol_draw_coordinate_system){
        //   //Ordinate
        //    g2d.drawLine(x_scale(i_ordinate_x), y_scale(i_ordinate_y), x_scale(i_ursprung_x), y_scale(i_ursprung_y));
        //    //Abszisse
        //    g2d.drawLine(x_scale(i_ursprung_x), y_scale(i_ursprung_y), x_scale(i_abszisse_x), y_scale(i_abszisse_y));
        //    //boolean wieder auf false setzen, damit nur einmal gezeichnet wird
        //    bol_draw_coordinate_system = false;
        //    System.out.println("Ursprung eingezeichnet");
        //}
        //g.drawRect(15,70,690,360);
        
        //Erfasste Funktionsstützwerte in Diagramm Vorschau zeichnen
        //if(bol_draw == true){
        //    //funktionspunkte zeichnen
        //    for(i = 0; i<i_support_point; i++){
        //        g2d.setStroke(new BasicStroke(2));
        //        g2d.drawLine(x_scale(arr_f_values_x_gui[i]), y_scale(arr_f_values_y_gui[i]), x_scale(arr_f_values_x_gui[i]), y_scale(arr_f_values_y_gui[i]-1));
        //    }
        //}
        
        //berechnetes Lagrange Polynom mittels Punkten im gui darstellen
        try{
            if(bol_draw_polynom){
                //zeichne Stützwerte
                g2d.setColor(Color.red);
                g2d.setStroke(new BasicStroke(10));
                for(i = 0; i<i_support_point; i++){
                    g2d.drawLine(x_scale(arr_f_values_x_gui[i] ), y_scale(arr_f_values_y_gui[i] ), x_scale(arr_f_values_x_gui[i] ), y_scale(arr_f_values_y_gui[i] ));
                }
                //zeichne Polynom
                g2d.setColor(Color.red);
                g2d.setStroke(new BasicStroke(3));
                for(i = 0; i<i_x_resolution-2; i++){
                    if((x_scale(arr_i_y_draw_polynom[i]) < y_scale(i_height) && y_scale(arr_i_y_draw_polynom[i]) > 0) && (y_scale(arr_i_y_draw_polynom[i+1]) < y_scale(i_height) && y_scale(arr_i_y_draw_polynom[i+1]) > 0)){
                        g2d.drawLine(x_scale(arr_i_x_draw_polynom[i]), y_scale(arr_i_y_draw_polynom[i]), x_scale(arr_i_x_draw_polynom[i+1]), y_scale(arr_i_y_draw_polynom[i+1]));
                    }
                }
                bol_draw_polynom = false;
            }
        }catch(ArrayIndexOutOfBoundsException e) {
                bol_draw_polynom = false;
                str_fail = "Lagrange Interpolation/Approximation ist Fehlgeschlagen";
                state = State.FAIL;
        }catch(NullPointerException n) {
                bol_draw_polynom = false;
                str_fail = "Lagrange Interpolation/Approximation ist Fehlgeschlagen";
                state = State.FAIL;
        }
       
        //zeichne Scanpunkt ins GUI
        if(bol_draw_scanpoint){
            g2d.setColor(Color.green);
            g2d.setStroke(new BasicStroke(10));
            g2d.drawLine(x_scale(0), y_scale(i), x_scale(i_width), y_scale(i));
            //System.out.println("Zeichne: "+j+" "+i);
        }
    }
    
    private static void waitProcessing(int iWait){
        try {
            Thread.sleep(iWait);
        }catch(InterruptedException ie) {
        }
    }
        
    /**
     * ActionListener
     */
    public void actionPerformed(ActionEvent e){
        // Open a Picture
        if(e.getSource() == btn_open_picture){
            state = State.OPEN_IMAGE;
        }
        //analyze the picture
        if(e.getSource() == btn_analyze_picture){
            state = State.ANALYZE;
        }
    }
    
     /**
     * ItemListener
     */
    public void itemStateChanged(ItemEvent f){
        //Listener for choosing a source
        //if(f.getSource() == jcSource){
        //}
    }
    
    /**
     * WindowListener Methoden
     */
    public void windowClosing(WindowEvent event){
        //setPreference();    //safe properties
        dispose();          //vanish window
        System.exit(0);     //end application
    }
    public void windowIconified(WindowEvent event){ 
    }
    public void windowOpened(WindowEvent event){
    }
    public void windowClosed(WindowEvent event){
    }
    public void windowActivated(WindowEvent event){
    }
    public void windowDeiconified(WindowEvent event){
    }
    public void windowDeactivated(WindowEvent event){
    }
    private void exitForm(WindowEvent event){
    }
}
