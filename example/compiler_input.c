#include<stdio.h>

int main(){

  int b;
  int y;
  int result;
  
  b=3;
  y=3;
  // compute b^y
  result=1;
  while (y>0){
    result=result*b;
    y=y-1;
  }
  
  printf("%d\n",result);
  return 0;
}
