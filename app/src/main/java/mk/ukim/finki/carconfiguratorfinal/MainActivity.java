package mk.ukim.finki.carconfiguratorfinal;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import mk.ukim.finki.carconfiguratorfinal.Dealer.DealerActivity;
import mk.ukim.finki.carconfiguratorfinal.Gallery.Gallery;
import mk.ukim.finki.carconfiguratorfinal.RetrofitClasses.Classes.MercedesClasses;
import mk.ukim.finki.carconfiguratorfinal.RetrofitClasses.Country.ConfigurationCountries;
import mk.ukim.finki.carconfiguratorfinal.RetrofitClasses.Model.Model;
import mk.ukim.finki.carconfiguratorfinal.RetrofitClasses.ModelDetails.CarEngine;
import mk.ukim.finki.carconfiguratorfinal.RetrofitClasses.ModelDetails.Configuration;
import mk.ukim.finki.carconfiguratorfinal.RetrofitClasses.ModelDetails.FuelEconomy;
import mk.ukim.finki.carconfiguratorfinal.Tasks.CarDetails.AsyncResponseCarDetails;
import mk.ukim.finki.carconfiguratorfinal.Tasks.CarDetails.DownloadCarDetails;
import mk.ukim.finki.carconfiguratorfinal.Tasks.Classes.AsyncResponseClasses;
import mk.ukim.finki.carconfiguratorfinal.Tasks.Classes.DownloadCarClasses;
import mk.ukim.finki.carconfiguratorfinal.Tasks.Country.AsyncResponse;
import mk.ukim.finki.carconfiguratorfinal.Tasks.Country.DownloadCarCounties;
import mk.ukim.finki.carconfiguratorfinal.Tasks.Models.AsyncResponseModel;
import mk.ukim.finki.carconfiguratorfinal.Tasks.Models.DownloadCarModels;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //TextView
    private TextView textViewCarEngine;
    private TextView textViewFuelEconomy;
    private TextView textViewTopSpeed;
    private TextView textViewNumberOfSeats;
    private TextView textViewPrice;
    private TextView textViewNetPrice;

    //List for the spinners
    private List<String> listMarketName = new ArrayList<>();
    private List<String> listCarClassName = new ArrayList<>();
    private List<String> listCarModelName = new ArrayList<>();

    //Spinners
    private Spinner marketSpinner ;
    private Spinner carClassSpinner;
    private Spinner carModelSpinner;

    //Async Task
    private DownloadCarCounties asyncTask;
    private DownloadCarClasses asyncTaskClasses;
    private DownloadCarModels asyncTastModel;
    private DownloadCarDetails asyncTaskCarDetails;

    //Results
    private List<ConfigurationCountries> list_countries;
    private ConfigurationCountries countryObject;
    private List<MercedesClasses> list_classes;
    private MercedesClasses classObject;
    private List<Model> modelList;
    private Model modelObject;
    private Configuration carConfiguration;

    //Variables
    private String api_key;
    private MainActivityService mainService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialization: mainService and getting the resources for the configuration api key.
        mainService = new MainActivityService();
        api_key = getString(R.string.api_key1_configuration);

        //Methods generated by android studio
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Function that initialize the spinners
        setupSpinners();

        //Function that makes an async call
        asyncCall();

        //Call to set the textViews
        setTextViews();
    }

    //Creating an async call to download the data about the country
    //For more information check the DownloadCarCountries class
    private void asyncCall(){
        asyncTask = (DownloadCarCounties) new DownloadCarCounties(new AsyncResponse() {
            @Override
            public void processFinish(List<ConfigurationCountries> output) {
                list_countries = output;
                fillSpinner(output);
            }
        }).execute(mainService.createArray(api_key));
    }

    //Sets the country spinner for clicking events
    private void setCountrySpinner(){
        this.marketSpinner = findViewById(R.id.market_spinner);
        ArrayAdapter<String> aAdpt = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listMarketName);
        aAdpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.marketSpinner.setAdapter(aAdpt);
    }

    //Fills the country spinner
    private void fillSpinner(List<ConfigurationCountries> list){
        //Adds the objects of ConfigurationCountries type to the spinner
        for (ConfigurationCountries item : list)
            listMarketName.add(item.getCountry());
        //Set the data in the spinner
        setCountrySpinner();
        //Handling the event when the spinner is clicked
        this.marketSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.onItemSelectedCountry(parent, view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Nothing is done
            }
        });
    }

    private void onItemSelectedCountry(AdapterView<?> parent, View view, int position, long id){
        String country = parent.getItemAtPosition(position).toString(); //country is a variables where we store the selected country from the spinner
        if (id > 0) {
            Toast.makeText(MainActivity.this, "You choose market: " + country, Toast.LENGTH_SHORT).show(); //Simple message to the user
            countryObject = mainService.findCountryObject(country, list_countries); //findCountryObject is a method in the service check for more details in the MainActivityService class
            String link = mainService.formatLink(countryObject.getAllLinks().getClassesLink()); //formatLink is a method in the service check for more details in the MainActivityService class
            List<String> list = new ArrayList<>(); //We create an array so that we can store the data that will be used in creating the second async call (Call for the classes of cars)
            list.add(link); //We add the format link (For the retrofit)
            list.add(api_key); //We also add the api key
            asyncTaskClasses = (DownloadCarClasses) new DownloadCarClasses(new AsyncResponseClasses() {
                @Override
                public void processFinish(List<MercedesClasses> output) {
                    list_classes = output;
                    fillClassSpinner(output); //When the data is collected from the second async call this function is called
                }
            }).execute(mainService.convertListToArray(list)); //Calling the second async task
        }
    }

    //setClassSpinnner sets the data in the spinner list inside the spinner so that the user can see it and work with it
    private void setClassSpinner(){
        this.carClassSpinner = (Spinner) findViewById(R.id.class_spinner);
        ArrayAdapter<String> aAdpt = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listCarClassName);
        aAdpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.carClassSpinner.setAdapter(aAdpt);
    }

    //fillClassSpinner is a method that sets the data, that is collected, in the spinners
    //and makes the third async call to get the models
    private void fillClassSpinner(List<MercedesClasses> list){
        listCarClassName.add("Select a Car Class");
        for(MercedesClasses mercedes_class: list) //We put the data in the spinner list
            listCarClassName.add(mercedes_class.getClassName());

        setClassSpinner(); //We set the data that is in the list spinner inside the spinner

        this.carClassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { //Handling the event clicking spinner
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.onItemClickClass(parent, view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    private void onItemClickClass(AdapterView<?> parent, View view, int position, long id){
        String carClass = parent.getItemAtPosition(position).toString();
        if (id > 0) {
            Toast.makeText(MainActivity.this, "You choose class: " + carClass, Toast.LENGTH_SHORT).show(); //Simple message to the users
            classObject = mainService.findCountryObjectByClass(carClass, list_classes); //Method that is in the MainActivityService class check for more information there
            String link = mainService.formatLink(classObject.getAllLinks().getModelsLink()); //Method that is in the MainActivityService class check for more information there
            List<String> list = new ArrayList<>(); //Simple list so the data that is requered can be stored and used in the next async call
            list.add(link);
            list.add("190");
            list.add(api_key);
            asyncTastModel = (DownloadCarModels) new DownloadCarModels(new AsyncResponseModel() {
                @Override
                public void processFinish(List<Model> output) {
                    modelList = output;
                    fillModelSpinners(output); //Function to work with the returned data
                }
            }).execute(mainService.convertListToArray(list)); //Async call to get the car models from a given class
        }
    }

    //setModelSpinner sets the data in the spinner list inside the spinner so that the user can see it and work with it
    private void setModelSpinner(){
        this.carModelSpinner = (Spinner) findViewById(R.id.model_spinner);
        ArrayAdapter<String> aAdpt = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, listCarModelName);
        aAdpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.carModelSpinner.setAdapter(aAdpt);
    }

    private void fillModelSpinners(List<Model> models){
        listCarModelName = new ArrayList<>();
        listCarModelName.add("Choose Car model");

        for(Model model: models) //Loop that puts the collected data in the spinner list
            listCarModelName.add(model.getModelName());

        setModelSpinner(); //Call for the setModelSpinner method
        this.carModelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { //When the spinner is clicked do onItemSelectedModel
                onItemSelectedModel(parent,view,position,id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        }); //Handling the events for the spinners
    }

    private void onItemSelectedModel(AdapterView<?> parent, View view, int position, long id){
        String carModel = parent.getItemAtPosition(position).toString();
        if(id > 0) {
            Toast.makeText(MainActivity.this, "Your choice for car model: " + carModel, Toast.LENGTH_SHORT).show();
            modelObject = mainService.findModel(carModel, modelList); //Method that is in the MainActivityService class check for more information there
            List<String> list = new ArrayList<>(); //Simple array for the data
            list.add(mainService.formatLink(modelObject.getModelLinks().getConfigurationLink())); //Method that is in the MainActivityService class check for more information there
            list.add(api_key);
            asyncTaskCarDetails = (DownloadCarDetails) new DownloadCarDetails(new AsyncResponseCarDetails() { //Making the last async task call
                @Override
                public void processFinish(Configuration output) {
                    carConfiguration = output;
                    Log.i("MainActivity", carConfiguration.getPrice().getCurrency());
                    fillConfigurationTextView();
                }
            }).execute(mainService.convertListToArray(list));

        }
    }

    //Method that sets the text views where the data about the car will be displayed
    private void setTextViews(){
        textViewCarEngine = findViewById(R.id.textViewCarEngine);
        textViewFuelEconomy = findViewById(R.id.textViewFuelEconomy);
        textViewTopSpeed = findViewById(R.id.textViewTopSpeed);
        textViewNumberOfSeats = findViewById(R.id.textViewNumberOfSeats);
        textViewPrice = findViewById(R.id.textViewPrice);
        textViewNetPrice = findViewById(R.id.textViewNetPrice);
    }

    //Method that fills the text views with the data collected about the car configuration.
    //All the method that are used in this function, except for createStringForTextView, are placed in the MainActivityService.
    //Check that class for more details about those methods.
    private void fillConfigurationTextView(){
        textViewCarEngine.setText(createStringForTextView(mainService.createNamesCarEngine(), mainService.createValuesCarEngine(carConfiguration), "Car Engine:", true));
        textViewFuelEconomy.setText(createStringForTextView(mainService.createNamesFuelEconomy(), mainService.createValuesFuelEconomy(carConfiguration), "Fuel economy:", true));
        textViewTopSpeed.setText(createStringForTextView(mainService.createNamesTopSpeed(), mainService.createValuesTopSpeed(carConfiguration), "Top speed", false));
        textViewNumberOfSeats.setText(createStringForTextView(mainService.createNamesNumberOfSeats(), mainService.createValuesNumberOfSeats(carConfiguration), "Number of seats:", false));
        textViewPrice.setText(createStringForTextView(mainService.createNamesPrice(), mainService.createValuesPrice(carConfiguration), "Price:", false));
        textViewNetPrice.setText(createStringForTextView(mainService.createNamesNetPrice(), mainService.createValuesNetPrice(carConfiguration), "Net-price:", false));
    }

    //createStringForTextView is a method that gets a list of names, a list of values, a field name and a boolean.
    //The list of names and values are list of string.
    //The field name is used so it can contain the data which has more then one name and value.
    //The boolean is used to tell us if the list has more than one name and value or not.
    private String createStringForTextView(List<String> names, List<String> values, String field, boolean displayField) {
        StringBuilder sb = new StringBuilder(); //Creating a string builder

        if(displayField) //Including or not the field name
            sb.append(field).append("\n");

        for (int i = 0; i < names.size(); i++) //Simple loop through the lists
            sb.append(names.get(i)).append("    ").append(values.get(i)).append("\n");

        sb.append("\n");

        return sb.toString(); //Return the string
    }

    //Function that sets the spinners
    private void setupSpinners(){
        this.listMarketName.add("Click to choose Market - Country");
        initSpinner(marketSpinner, (R.id.market_spinner), listMarketName);
        initSpinner(carClassSpinner, (R.id.class_spinner), listCarClassName);
        initSpinner(carModelSpinner, (R.id.model_spinner), listCarModelName);
    }

    //Function that initialize the spinners
    private void initSpinner(Spinner spinner, int viewid,  List<String> items){
        spinner = (Spinner) findViewById(viewid);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    //Methods automatically generated by android studio for the work with the side menu
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //Each id represents a button in the menu.
        //On click event it opens a new activity in which data is shown.
        if (id == R.id.nav_gallery) { //Gallery button
            Intent gallery_intent = new Intent(MainActivity.this, Gallery.class);
            startActivity(gallery_intent);
        } else if (id == R.id.nav_dealer) { //Dealer button
            Intent dealer_intent = new Intent(MainActivity.this, DealerActivity.class);
            startActivity(dealer_intent);
        } else if (id == R.id.nav_news) { //News button
            Intent news_intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://developer.mercedes-benz.com/news"));
            startActivity(news_intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}