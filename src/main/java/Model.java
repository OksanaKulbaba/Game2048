import java.util.*;


public class Model {
    private static final int FIELD_WIDTH = 4;

    private Tile[][] gameTiles;
    protected int score;
    protected int maxTile;

    private Stack previousStates = new Stack();
    private Stack previousScores = new Stack();
    public boolean isSaveNeeded = true;

    private void  saveState (Tile[][] tile){
        Tile[][] tile2 = new Tile[tile.length][tile[0].length];
        for (int i = 0; i < tile.length; i++) {
            for (int j = 0; j < tile[0].length; j++) {
                tile2[i][j] = new Tile(tile[i][j].value);
            }
        }
        previousStates.push(tile2);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback(){
        if (!previousStates.isEmpty()&& !previousScores.isEmpty()){
        gameTiles =(Tile[][]) previousStates.pop();
        score  = (int) previousScores.pop();}

    }
    public Model() {
        score = 0;
        maxTile= 2;
        resetGameTiles();
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }
    public boolean canMove(){
        if (!getEmptyTiles().isEmpty())
            return true;
        for(int i = 0; i < gameTiles.length; i++) {
            for(int j = 1; j < gameTiles.length; j++) {
                if(gameTiles[i][j].value == gameTiles[i][j-1].value)
                    return true;
            }
        }
        for(int j = 0; j < gameTiles.length; j++) {
            for (int i = 1; i < gameTiles.length; i++) {
                if (gameTiles[i][j].value == gameTiles[i - 1][j].value)
                    return true;
            }
        }
        return false;
    }

    private void addTile(){
        List<Tile> list  = getEmptyTiles();
        if (list.size()!=0){
        int random  =(int) (Math.random()*list.size());
        list.get(random).value = (Math.random()<0.9)?2:4;}


    }
    private List<Tile> getEmptyTiles(){
        List<Tile> list = new ArrayList<>();

        for(int i =0; i <FIELD_WIDTH ; i++){
            for (int j =0; j<FIELD_WIDTH; j++){
                if (gameTiles[i][j].isEmpty())
                    list.add(gameTiles[i][j]);
            }
        }

        return list;
    }
    protected void resetGameTiles(){
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
          for(int i =0; i <FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = new Tile();
            }
        }

        addTile();
        addTile();

    }
    private boolean compressTiles(Tile[] tiles){
        boolean b = false;
        for (int i = 0; i < tiles.length-1; i++) {
            if(tiles[i].value==0&&i<tiles.length-1&&tiles[i+1].value!=0){
                Tile temp = tiles[i];
                tiles[i] = tiles[i+1];
                tiles[i+1] = temp;
                i=-1;
                b = true;
            }
        }
        return b;
    }
    private boolean mergeTiles(Tile[] tiles){
        boolean b = false;
        for (int i=0; i<tiles.length-1; i++){
            if (tiles[i].value !=0 && tiles[i].value==tiles[i+1].value){
                tiles[i].value = tiles[i].value*2;
                tiles[i+1].value = 0;
                b = true;
                if (maxTile <tiles[i].value ){
                    maxTile =  tiles[i].value;


                }
                score += tiles[i].value;

            }
        }
        if (b)  compressTiles(tiles);
        return b;
    }
    protected void left(){
        if (isSaveNeeded){
            saveState(gameTiles);
        }
        boolean isChanged = false;
        for (int i = 0; i < FIELD_WIDTH; i++) {
            boolean compress = compressTiles(gameTiles[i]);
            boolean merge = mergeTiles(gameTiles[i]);
            if(compress || merge)
            {
                isChanged=true;
            }
        }
        if (isChanged) addTile();
        isSaveNeeded =true;
    }
    protected void up(){
        saveState(gameTiles);
        canche(1);
        left();
        canche(3);


    }
    protected void right(){
        saveState(gameTiles);
       canche(2);
       left();
       canche(2);


    }
    protected void down(){
        saveState(gameTiles);
        canche(3);
        left();
        canche(1);


    }

    private void canche(int m){
        for (int l =0; l <m ; l++) {
            for (int i = 0; i < FIELD_WIDTH / 2; i++) {
                for (int j = i; j < FIELD_WIDTH - 1 - i; j++) {
                    Tile tmp = gameTiles[i][j];
                    gameTiles[i][j] = gameTiles[j][FIELD_WIDTH - 1 - i];
                    gameTiles[j][FIELD_WIDTH - 1 - i] = gameTiles[FIELD_WIDTH - 1 - i][FIELD_WIDTH - 1 - j];
                    gameTiles[FIELD_WIDTH - 1 - i][FIELD_WIDTH - 1 - j] = gameTiles[FIELD_WIDTH - 1 - j][i];
                    gameTiles[FIELD_WIDTH - 1 - j][i] = tmp;
                }
            }
        }
    }
    public void randomMove(){
      int  n = ((int) (Math.random() * 100)) % 4;
        switch (n){
            case 0:
                left();
                break;
            case  1:
                up();
                break;
            case 2:
                down();
                break;
            case 3:
                right();
                break;
        }
    }

    public boolean hasBoardChanged(){
        int m =0;
        int n =0;
        m = value(gameTiles);
        n = value((Tile[][])previousStates.peek());
        if (m!=n) return true;


        return  false;
    }
    public int value (Tile[][] tile) {
        int m =0;
        for (int i = 0; i < FIELD_WIDTH; i++ ){
            for (int j =0; j <FIELD_WIDTH; j++){
            m += tile[i][j].value;
            }
        }

        return m;
    }


    public MoveEfficiency getMoveEfficiency(Move move){

        MoveEfficiency moveEfficiency;
        move.move();
        if (hasBoardChanged())
        {moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);}
        else moveEfficiency = new MoveEfficiency(-1, 0, move);
        rollback();
        return moveEfficiency;
    }
    public void autoMove(){

        PriorityQueue<MoveEfficiency> prior= new PriorityQueue<>(4, Collections.reverseOrder());
        prior.offer(getMoveEfficiency(this::left));
        prior.offer(getMoveEfficiency(this::right));
        prior.offer(getMoveEfficiency(this::up));
        prior.offer(getMoveEfficiency(this::down));

        prior.peek().getMove().move();


    }
}

