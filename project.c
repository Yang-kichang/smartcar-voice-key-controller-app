#include<stdio.h>
#include<stdlib.h>

int main(void)
{
    char type,int_num[10][50],float_num[10][9],syntax[10];
    int i,n;

    i=0,n=0;

    while((type=getchar())!='\n')
    {
        switch(type)
        case '*':
        case '%':
        case '/':
        case '+':
        case '-':
            syntax[i]=type;
            i++;

            break;

        case '.':
            float_num[10][n]=type;
            n++;

            break;

        default:

            break;
    }



