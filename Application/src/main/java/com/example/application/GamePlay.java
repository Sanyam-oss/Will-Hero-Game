package com.example.application;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.*;

public class GamePlay implements Serializable {
    private Hero hero;
    private ArrayList<Island> islands;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<Reward> rewards;
    private ArrayList<Weapon> weaponInstances;
    transient AnimationTimer animator;
    private long dashTime;
    private final Random random ;
    private ImageView crown_Img ;
    //private int cnt = 0 ;
    private boolean pause;
    private transient Game game;
    private transient SceneController st;
    private transient ArrayList<ImageView> clouds;
    private transient double[] mov = new double[]{ 0.8,0.8,0.8,0.85,0.85};

    @FXML
    private transient AnchorPane game_pane;

    @FXML
    private transient Group pauseGroup;

    @FXML
    private transient Group savingGroup;

    @FXML
    private transient Group mainGroup;
    @FXML
    private transient TextField tf;
    @FXML
    private transient Text message;

    @FXML
    private transient Text score ;

    @FXML
    private transient ImageView cloud_1;

    @FXML
    private transient ImageView cloud_2;

    @FXML
    private transient ImageView cloud_3;

    @FXML
    private transient ImageView cloud_4;

    @FXML
    private transient ImageView cloud_5;

    public GamePlay(){

        islands = new ArrayList<Island>();
        obstacles = new ArrayList<Obstacle>();
        rewards = new ArrayList<Reward>();
        clouds = new ArrayList<ImageView>();
        crown_Img = new ImageView(new Image("crownn.gif"));
        random = new Random();
        pause = false;
        setup_Game();
        dashTime = System.nanoTime();
        st = new SceneController();
        weaponInstances = new ArrayList<Weapon>();


        animatorLogic();
    }
    private void animatorLogic(){
        animator = new AnimationTimer(){

            @Override
            public void handle(long now) {

                //-----HERO--------
                if(!hero.isAlive()) {
                    System.out.println(hero.getLocation().getY());
                    System.out.println("Hero died!");              // Show end menu
                    try {
                        animator.stop();
                        int points = hero.getCollectedCoins() ;
//                        hero = new Hero(300,120);
//                        hero.setCollectedCoins(points);
                        hero.setLocation(300,120);
                        hero.updateLocation();
                        hero.setYspeed(0);
                        hero.setAlive(true);

                        showEndMenu(game_pane);
                        return ;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Normal Gravity on hero
                if(dashTime <= now) {
                    hero.setXspeed(0);
                    hero.setYspeed(hero.getYspeed() + 0.3);
                    hero.gravityEffect();

                } else {
                    hero.setYspeed(0.5);          // To restrict unlimited flying
                    hero.gravityEffect();
                }

                //-----Animate weapons------
                for(Weapon weapon : weaponInstances){
                    if(weapon.use()){
                        weapon.gravityEffect();
                    }
                }

                //------remove animated weapons-----
                Iterator<Weapon> itr = weaponInstances.iterator();
                while(itr.hasNext()){

                    Weapon wi = itr.next();
                    if(!wi.use()){  itr.remove(); wi.undisplay(game_pane); }

                }

                //-------OBSTACLES and ISLANDS----------

                // Gravity on obstacles
                for (Obstacle obs : obstacles) {
                    if (obs instanceof Orcs) {
                        ((Orcs) obs).setYspeed(((Orcs) obs).getYspeed() + 0.35);
                    }
                }

                // Checking for Dash
                int dashSpeed = 70 ;
                if(dashTime > now){
                    // Make all the objects move backwards
                    for(Obstacle obs: obstacles){
                        Location curr = obs.getLocation();
                        obs.setLocation(curr.getX()-dashSpeed,curr.getY());
                    }
                    for(Island currIsland : islands){
                        Location curr = currIsland.getLocation();
                        currIsland.setLocation(curr.getX()-dashSpeed,curr.getY());
                        currIsland.updateLocation();
                    }
                    for(Reward reward: rewards){
                        Location curr = reward.getLocation();
                        reward.setLocation(curr.getX()-dashSpeed,curr.getY());
                        reward.updateLocation();
                    }

                    crown_Img.setX(crown_Img.getX()-dashSpeed);
                }

                for(Obstacle obs: obstacles){
                    obs.gravityEffect();
                }

                //---------COLLISION CHECKS-------------
                // Jumps on Islands
                for (Island currIsland : islands) {

                    //check collision with hero
                    if (currIsland.checkCollision(hero)) {
                        currIsland.ifHeroCollides(hero);
                    }

                    //check collision with obstacles
                    for(Obstacle obs: obstacles){
                        if(currIsland.checkCollision(obs)) {
                            currIsland.ifObstacleCollides(obs);
                        }
                    }
                }
                //hero vs reward and obstacles
                for(Reward reward: rewards){
                    if(reward.checkCollision(hero)){
                        reward.ifHeroCollides(hero);
                        hero.displayWeapon(game_pane);
                    }
                }
                for(Obstacle obs: obstacles){
                    if(obs.checkCollision(hero)){
                        dashTime = dashTime/100;
                        obs.ifHeroCollides(hero);
                    }
                    for(Weapon we: weaponInstances){
                        if(obs.checkCollision(we)){
                            obs.ifWeaponCollides(we);
                            we.setLifeTime(0);
                        }
                    }
                }
                //--------Obstacle vs Obstacle-----------
                for( int i=0 ; i < obstacles.size() ; i++ ){
                    if(obstacles.get(i) instanceof TNT){ continue ; }
                    for( int j=i+1 ; j < obstacles.size() ; j++){
                        if(obstacles.get(i) instanceof TNT){ continue ; }
                        Obstacle orc1 = obstacles.get(i);
                        Obstacle orc2 = obstacles.get(j);
                        if(orc1.checkCollision(orc2)){
                            System.out.println(i+" "+j);
                            orc1.ifObstacleCollides(orc2);
                        }
                    }
                }

                //REMOVAL of OBSTACLES
                for(Obstacle obs : obstacles){
                    if(!obs.isAlive()){
                        // obs.getCoins();              //  1
                        hero.setCollectedCoins(hero.getCollectedCoins()+1);
                    }
                }

                Iterator<Obstacle> itr1 = obstacles.iterator();

                while(itr1.hasNext()){
                    Obstacle obs = itr1.next();
                    if(!obs.isAlive()){  obs.undisplay(game_pane) ; itr1.remove(); }
                }

                score.setText(hero.getCollectedCoins() + " Coins");

                ImageView iv ;

                for(int i=0 ; i<clouds.size(); i++){
                    iv = clouds.get(i);
                    double pos = iv.getLayoutX()-mov[i];
                    if(pos <= 0){ pos = 1400 ;}
                    iv.setLayoutX(pos);
                }
            }
        };
    }

    public void setup_Game(){

        hero = new Hero(300.0,230.0);
        //rewards.add(new CoinChest(2800,310));
//        rewards.add(new WeaponChest(2800,310,new ThrowingKnife(0,0))) ;
//        rewards.add(new WeaponChest(3900,310,new Sword(0,0))) ;

        int x = 250;

        String []Large_Island_Images = {"island1.png","island_large1.png","island_large2.png"};
        String []Small_Island_Images = {"island_med.png","island_small.png"};

        //obstacles.add(new StdOrc(x+600,26));

//        for(int i=1;i<=2;i++){
//
//            ArrayList<Integer>ind1 = new ArrayList<Integer>() ;
//            ArrayList<Integer>ind2 = new ArrayList<Integer>() ;
//
//            for(int k=1;k<=3;k++){ ind1.add(random.nextInt(Large_Island_Images.length)); }
//            for(int k=1;k<=2;k++){ ind2.add(random.nextInt(Small_Island_Images.length)); }
//
//            islands.add(new Island( x+0 ,368,358,126,Large_Island_Images[ind1.get(0)]));
//            islands.add(new Island(x+500,363,358,126,Small_Island_Images[ind2.get(0)]));
//            obstacles.add(new RedOrc(x+600,126));
//
//            islands.add(new Island(x+950,368,358,126,Large_Island_Images[ind1.get(1)]));
//            islands.add(new Island(x+1450,375,358,126,Small_Island_Images[ind2.get(1)]));
//
//            obstacles.add(new StdOrc(x+1550,126));
//
//            islands.add(new Island(x+1900,364,358,160,Large_Island_Images[ind1.get(2)]));
//            obstacles.add(new StdOrc(x+2000,126));
//
//            x+=2450 ;
//        }

        islands.add(new Island(x+0,364,358,160,Large_Island_Images[0]));
        islands.add(new Island(x+300,364,358,160,Large_Island_Images[1]));
        islands.add(new Island(x+600,364,358,160,Large_Island_Images[2]));
        islands.add(new Island(x+900,364,358,160,Large_Island_Images[1]));

        obstacles.add(new RedOrc(x+500,120));
        //obstacles.add(new RedOrc(x+600,120));
        obstacles.add(new BossOrc(x+800,100));

        islands.add(new Island(x+1700,364,358,160,Large_Island_Images[1]));
        islands.add(new Island(x+2000,364,358,160,Large_Island_Images[0]));
        rewards.add(new CoinChest(x+2100,310));

        islands.add(new Island(x+10000,364,358,160,Large_Island_Images[1]));

//        obstacles.add(new TNT(x-4000,295));
//        islands.add(new Island(x+10000,364,358,160,Large_Island_Images[1]));
//
//        islands.add(new Island(x,364,358,160,"island_large2.png"));
//        rewards.add(new WeaponChest(x-270,310,new ThrowingKnife(0,0))) ;
//        //TNT
//        obstacles.add(new TNT(3950,295));
    }

    public void set_Crown(){

        Island is = islands.get(islands.size()-2);
        double x = is.getLocation().getX();
        crown_Img.setX(x+70);
        crown_Img.setY(220);
        crown_Img.setFitHeight(110);
        crown_Img.setFitWidth(170);
    }

    public void heroDash(MouseEvent e){
        dashTime = System.nanoTime() + 120000000;

        if(hero.getCurrentWeapon()!=null){
            Weapon temp = hero.getCurrentWeapon() ;
            weaponInstances.add(temp) ;
            temp.display(game_pane);
        }
        // System.out.println(cnt++);
    }
    public void showPauseMenu(MouseEvent e) throws IOException {
        animator.stop();
        pauseGroup.setDisable(false);
        st.fade(pauseGroup,300,1).play();
        st.fade(mainGroup,300,0.7).play();
    }

    public void resume(ActionEvent e) throws IOException {
        animator.start();
        pauseGroup.setDisable(true);
        exitSaveScreen(null);
        SceneController st = new SceneController();
        st.fade(pauseGroup,300,0).play();
        st.fade(mainGroup,300,1).play();
    }
    public void showEndMenu(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("EndGameMenu.fxml")));
        Parent root = loader.load();
        Endgame ender = loader.getController();
        ender.setGamePlay(this);
        Stage stage = (Stage)((Node)e.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void showEndMenu(Node e) throws IOException{
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("EndGameMenu.fxml")));
        Parent root = loader.load();
        Endgame ender = loader.getController();
        ender.setGamePlay(this);
        Stage stage = (Stage)(e.getScene().getWindow());
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void showSaveScreen(ActionEvent e){
        this.savingGroup.setDisable(false);
        st.fade(savingGroup,300,1).play();
    }
    public void saveCurrentGame(ActionEvent e) throws IOException {
        if(tf.getText().equals("")){
            message.setText("Empty file name");
        } else {
            message.setText(this.game.addGamePlay(tf.getText(),this));
        }
    }
    public void exitSaveScreen(ActionEvent e) throws IOException {
        this.savingGroup.setDisable(true);
        st.fade(savingGroup,300,0).play();
    }

    public void copy(GamePlay gp) throws IOException {
        this.islands = gp.islands;
        this.hero = gp.hero;
        this.dashTime = gp.dashTime;
        this.obstacles = gp.obstacles;
        this.rewards = gp.rewards;
        this.pause = gp.pause;
        this.game = gp.game;
        this.reinitialize();
    }
    public void setGame(Game ga) {
        this.game = ga;
    }

    public int getScore(){
        return hero.getCollectedCoins();
    }
    public void reinitialize() throws IOException {

        if(game_pane != null){
            System.out.println("Game_pane is not null");
            hero.display(game_pane);
            for (Island island : islands) island.display(game_pane);
            for(Obstacle obs: obstacles) obs.display(game_pane);
            for(Reward rew: rewards) rew.display(game_pane);
            set_Crown();
            game_pane.getChildren().add(crown_Img);
            clouds.add(cloud_1); clouds.add(cloud_2); clouds.add(cloud_3); clouds.add(cloud_4); clouds.add(cloud_5);
            pauseGroup.setDisable(true);
            score.setText(hero.getCollectedCoins()+" Coins");
            if(!pause) animator.start();
            else {
                showPauseMenu(null);
            }
        }
    }
    public void changeWeaponToSword(MouseEvent e){
        this.hero.chooseWeapon("Sword",game_pane);
    }
    public void changeWeaponToThrowingKnife(MouseEvent e){
        this.hero.chooseWeapon("ThrowingKnife",game_pane);
    }
}
