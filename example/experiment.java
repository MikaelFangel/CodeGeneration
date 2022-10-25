class comp{
    public static void main(String [] argv){
	int b=3;
	int y=1000000000;
	// compute b^y
	int result=1;
	while (y>0){
	    result=result*b;
	    y=y-1;
	}
	System.out.println(result);
    }

}

