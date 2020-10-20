package com.ps.roulette;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.regex.Pattern;

public class RouletteGame extends Thread {

    Vector<String> vecPlayerList= new Vector<String>();
    static HashMap<String, ArrayList> betUsersMap = new HashMap<String, ArrayList>();
    static Scanner scanner;

    public static void main(String[] args) {
        final long sleeptimer = 30 * 1000;

        RouletteGame spinThread = new RouletteGame();

        try {
            spinThread.start();

            //System.out.println("Thread Started");

            //30 seconds to receive the user bets
            spinThread.sleep(sleeptimer);
            spinThread.interrupt();
            scanner.reset();
            scanner.close();
            //System.out.println("Thread Interrupted");

            /* Generate the Roulette Number */
            int rouletteNum = getRouletteNumber();

            //System.out.println("Generated Roulette Number :" + rouletteNum);

            //validateWinner
            computeResults(rouletteNum);

        } catch (Exception e) {
            System.out.println("Thread Running Exception" + e.getMessage());
        }
    }

        @Override
        public void run(){
            //System.out.println("Thread Started Running");
            //Get the Registered Players from the file
            fetchPlayersData();

            //System.out.println("Thread Running");

            while (!Thread.interrupted()) {
                getBetInputs(vecPlayerList);
            }
            //System.out.println("Exiting While loop");

        }

    public void fetchPlayersData() {
        try {
            BufferedReader file = new BufferedReader(new FileReader("players.txt"));
            String strPlayerName;

            while ((strPlayerName = file.readLine()) != null) {
                vecPlayerList.add(strPlayerName.trim());
            }
            file.close();
        } catch (IOException e) {
            System.out.println("Error while reading file: "+e.toString());
        }//Try-catch ends

        for(int iVecCtr=0; iVecCtr<vecPlayerList.size(); iVecCtr++)
            System.out.println("Index: "+iVecCtr+"--"+vecPlayerList.get(iVecCtr));
    }//fetchPlayersData ends


    public void getBetInputs(Vector<String> userNames) {
            final String SPLCHARS = "\\s*(\\s|=>|,)\\s*";

            scanner = new Scanner(System.in);

            System.out.println("Provide Console Input For Bets");

            for (int i=0; i<userNames.size(); i++) {
                System.out.println("<"+(i+1)+"> :"+userNames.get(i));
            }

            System.out.println("Select the user <Sequence Number>");
            String betUserNumString;
            int userSeq=-1;

            while (true) {
                betUserNumString=scanner.nextLine();

                if (betUserNumString != null){
                    try {
                        userSeq = Integer.parseInt(betUserNumString);
                    }catch (Exception e){
                        System.out.println("Invalid Option!! Select a valid user <Sequence Number>");
                        continue;
                    }
                    if ((userSeq >0) && (userSeq <= userNames.size())) {
                        break;
                    } else {
                        System.out.println("Invalid Option!! Select a valid user <Sequence Number>");
                        continue;
                    }
                }
                System.out.println("Invalid Option!! Select a valid user <Sequence Number>");
                continue;
            }

            System.out.println("Enter the Bet Options for " +userNames.get(userSeq-1)+"\nBetting-Input-Options<ODD, EVEN, or 1 to 36> Betting-Amount");

            String betValue = scanner.nextLine();

            Pattern p = Pattern.compile(SPLCHARS);
            String[] items;
            double betOp=0, betVal=0;

            if (betValue != null) {
                items = p.split(betValue);

                if (items[0].equalsIgnoreCase("even"))
                    betOp = -2.0;
                else if (items[0].equalsIgnoreCase("odd"))
                    betOp = -1.0;
                else
                    betOp = Double.parseDouble(items[0]);

                betVal = Double.parseDouble(items[1]);
            }

//            System.out.println("Input For Bets Received"+betValue);
//            System.out.println("Input For Bets Option: "+betOp);
//            System.out.println("Input For Bets Value: "+betVal);

            //Parameters user-name, Bet Option, Bet Amount
            recordBets(userNames.get(userSeq-1), betOp, betVal);

        }

        public void recordBets(String userName, double betOp, double betAmt) {
            betUsersMap.put(userName, addBetRecordToUserMap(betUsersMap, userName, betOp, betAmt));
        }

    public ArrayList addBetRecordToUserMap(HashMap<String, ArrayList> betMap, String betUser, Double betOpt, double betAmt) {
        ArrayList<Double> betOpRecord = new ArrayList<>();
        ArrayList<ArrayList> tempBetOptionList = new ArrayList<>();

        betOpRecord.add(0,betOpt);
        betOpRecord.add(1,betAmt);
        betOpRecord.add(2, -1.0); //Results Not Announced; will be updated by the "spin" routine. -1.0 bet option did not win
        betOpRecord.add(3, 0.0); //Winning Amount; will be updated by the "spin" routine

        if (betMap.get(betUser) != null) {
            //System.out.println("Inside Array list update-getUser NOT-Null");
            //System.out.println("Bet User:"+betMap.get(betUser));
            //System.out.println(betUser+"\t"+betMap.get(betUser));
            tempBetOptionList = betMap.get(betUser);

            //tempBetOptionList.add(betMap.get(betUser));
            //System.out.println("Existing tmpBetList:\t" + tempBetOptionList);
            tempBetOptionList.add(betOpRecord);
            //System.out.println("Updated tmpBetList:\t" + tempBetOptionList);
        } else {
            //System.out.println("Inside Array list update-getUser IS-Null");
            tempBetOptionList.add(betOpRecord);
        }

        betMap.replace(betUser,tempBetOptionList);

        //System.out.println(betUser+"\t"+tempBetOptionList);

        return tempBetOptionList;
    }

        public static int getRouletteNumber() {
            SecureRandom secRand = new SecureRandom();
            Random rand = new Random();
            int rn = 0;

            //Using SecureRandom to reduce the predictability
            //Also generating it in random number of times
            for (int i = 0; i <= (rand.nextInt(10)+1); i++) {
                rn = 1 + Math.abs(secRand.nextInt(36));
                //System.out.print(rn+"\t");
            }

            return rn;
        }

    /*Spins, computes wins/losses, displays results */
    public static void computeResults(int rn) {
        System.out.println("Computing Results");

        ArrayList<Double> betResRecord = new ArrayList<>();
        ArrayList<ArrayList> tempBetResList = new ArrayList<>();

        String strBetDisp, strResDisp;
        boolean bOdd = false;


        //Mark result as even or odd
//        if (rn%2==0)
//            bOdd = false;
//        else
//            bOdd = true;

        bOdd = ((rn%2==0) ? false : true);

        System.out.println("Number :" + rn);
        System.out.println("Player\t\tBet\t\tOutcome\t\tWinnings"); // Display header
        System.out.println("---------------------------------------------------------------");

        //Iterate map and update results in map
        for(HashMap.Entry<String, ArrayList> collBetMap : betUsersMap.entrySet()){
            //System.out.println(collBetMap.getKey() + "--"+ collBetMap.getValue());
            tempBetResList = collBetMap.getValue();

            //iterate thru each bet record and update results
            for (int iArrCtr=0; iArrCtr<tempBetResList.size(); iArrCtr++) {
                betResRecord = tempBetResList.get(iArrCtr);
                //dWinLoss = betResRecord.get(3);
                //System.out.println("dWinLoss:"+betResRecord.get(1).toString());
                if ((betResRecord.get(0) == -1 && bOdd) ||
                        (betResRecord.get(0) == -2 && !bOdd)) // user chose odd number and odd number wins
                {
                    betResRecord.set(2,1.0); //Mark as won
                    betResRecord.set(3, betResRecord.get(1)*2); // double winnings
                }
                else if (betResRecord.get(0)==rn) {
                    betResRecord.set(2,1.0); //Mark as won
                    betResRecord.set(3, betResRecord.get(1)*36); // double winnings
                }
                else {
                    betResRecord.set(2,-1.0); //Mark as loss
                    betResRecord.set(3, 0.0); // zero winnings
                }
                tempBetResList.set(iArrCtr, betResRecord); // update record in array list

                //Display result for each iteration
                if (betResRecord.get(0)==-1)
                    strBetDisp = "ODD";
                else if(betResRecord.get(0)==-2)
                    strBetDisp = "Even";
                else
                    strBetDisp = betResRecord.get(0).toString();

                if (betResRecord.get(2)==-1.0)
                    strResDisp = "LOSS";
                else
                    strResDisp = "WIN";

                System.out.println(collBetMap.getKey()+"\t\t"+
                        strBetDisp+"\t\t"+
                        strResDisp+"\t\t"+
                        betResRecord.get(3).toString()
                );

            }//tempBetOptionsList iteration ends
            //Update betUsersMap with results for each key
            betUsersMap.replace(collBetMap.getKey(), tempBetResList);

        }//Map Entry set Iteration ends


    }


    }


