; ModuleID = 'generatedLLVMcode'

@.str = private constant [4 x i8] c"%d\0A\00"

@.str2 = private constant [4 x i8] c"%f\0A\00"

define void @print(i64 %d) nounwind ssp {
  %1 = alloca i64
  store i64 %d, i64* %1
  %2 = load i64, i64* %1
  %cast210 = getelementptr inbounds [4 x i8], [4 x i8]* @.str, i64 0, i64 0
  %3 = call i64 (i8*, ...) @printf(i8* %cast210, i64 %2)
  ret void
}

define void @printd(double %d) nounwind ssp {
  %1 = alloca double
  store double %d, double* %1
  %2 = load double, double* %1
  %cast210 = getelementptr inbounds [4 x i8], [4 x i8]* @.str2, i64 0, i64 0
  %3 = call i64 (i8*, ...) @printf(i8* %cast210, double %2)
  ret void
}

declare i64 @printf(i8*, ...)
declare i8* @malloc(i64) #1

define i64 @main() nounwind ssp {

%1 = call i8* @malloc(i64 0)
%b = bitcast i8* %1 to i64*
%2 = call i8* @malloc(i64 0)
%y = bitcast i8* %2 to i64*
%3 = call i8* @malloc(i64 0)
%result = bitcast i8* %3 to i64*
%4= add i64 0, 3
store i64 %4, i64* %b
%5= add i64 0, 3
store i64 %5, i64* %y
%6= add i64 0, 1
store i64 %6, i64* %result
%7= load i64, i64* %result
%8= load i64, i64* %b
%9= mul nsw i64 %7, %8
store i64 %9, i64* %result
%10= load i64, i64* %result
call void @print(i64 %10)
ret i64 0
}

