package com.nineinfosys.android.gstcalculator;

/**
 * Created by Dev on 17-03-2017.
 */

public class GstCalculator {

    double amount;
    double rate;
    String select;

    Gstcalculator values;

    GstCalculator(double amount,double rate,String select){

        this.amount=amount;
        this.rate=rate;
        this.select=select;

    }


    public  Gstcalculator getGstcalculators(){

        values=new Gstcalculator();

        values.setGstamount(calGst1());
        values.setNetprice(netprice());

        return values;
    }




    public double netprice() {

        if(select=="Add GST"){

            return amount +calGst1();

        } else {

            return amount - calGst1();
        }

    }
    public double calGst1() {

        if(select=="Add GST"){

            double GSTAmount = (amount * rate ) / 100 ;

            return GSTAmount;

        } else {

            double GSTAmount1 = amount - ( amount * ( 100 / ( 100 + rate ) ) );

            return GSTAmount1;
        }



    }
    class Gstcalculator {

        private double Gstamount;
        private double netprice;

        public double getGstamount() {
            return Gstamount;
        }
        public void setGstamount(double gstamount) {
            Gstamount = gstamount;
        }
        public double getNetprice() {
            return netprice;
        }
        public void setNetprice(double netprice) {
            this.netprice = netprice;
        }


    }


}
