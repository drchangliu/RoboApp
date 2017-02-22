%----------------------------------------------------------------------------------
%Ryan Lucas 
%10/16/2015
%Robocat project 
%RRSSR parallel robot IPK
%----------------------------------------------------------------------------------
clc; clear all;

port='COM5';
ser1 = serial(port);
set(ser1, 'InputBufferSize', 2048);
set(ser1, 'BaudRate', 9600);
set(ser1, 'DataBits', 8);
set(ser1, 'Parity', 'none');
set(ser1, 'StopBits', 1);

n=input('Input the number of steps:');

for (count=1:n)
    
knee=[10 20 30 50 70 80];
for (i=1:numel(knee))
    
    AngL = left_angles([-10 0 knee(i)]); %prompt user for angles
    AngR = right_angles([-10 0 knee(i)]);
   %=========OUTPUT OF THE CALCULATED ANGLES==========
   
   ShdangleL=AngL(1);
   LatangleL=AngL(2);
   KneangleL=AngL(3) ;        %prints the required angle of the lateral servo
   
   ShdangleR=AngR(1);
   LatangleR=AngR(2);
   KneangleR=AngR(3);
   
   if (imag(ShdangleL)~=0 || imag(LatangleL)~=0)
       fprintf(1,'Imaginary Angle!!!!!!!!\n')
       break
   end
    
    channel=1;              %initial channel
    no=3;                   %number of channels (in chain) to control
    device = 12;
    
    ShdBLpulseL=round(5700+ShdangleL*2000/90) ;
    KneBLpulseL=round(7000-KneangleL*5000/90);
    LatBLpulseL=round(6100+LatangleL*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseL, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseL, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseL, 8:14))), '[^\w'']', ''));
    
    command1 = [170, device, 31, no, channel, lower1, upper1, lower2, upper2, lower3, upper3]; % Modification for multiple servos
    fopen(ser1);
    
    channel
    lower1
    upper1
    lower2
    upper2
    lower3
    upper3
    
    % Send the command
    fwrite(ser1, command1);
    
    %Right front leg command
    ShdBLpulseR=round(5500-ShdangleR*2000/90); %Config for  right leg
    KneBLpulseR=round(5800+KneangleR*2000/90);
    LatBLpulseR=round(6100-LatangleR*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseR, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseR, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseR, 8:14))), '[^\w'']', ''));
    
    command = [170, device, 31, 3, 16, lower1, upper1, lower2, upper2, lower3, upper3]; % Modification for multiple servos
    
    16
    lower1 
    upper1
    lower2 
    upper2
    lower3 
    upper3
    
    % Send the command
    fwrite(ser1, command);
    fclose(ser1);
end

shoulder=[-10 10 20 40 50];

for (j=1:numel(shoulder))
    
   AngL =left_angles([shoulder(j) 0 knee(i)]); %prompt user for angles
   AngR = right_angles([shoulder(j) 0 knee(i)]);

   %=========OUTPUT OF THE CALCULATED ANGLES==========
  
   ShdangleL=AngL(1);
   LatangleL=AngL(2); 
   KneangleL=AngL(3);         %prints the required angle of the lateral servo
   
   ShdangleR=AngR(1);
   LatangleR=AngR(2);
   KneangleR=AngR(3);
   
   if (imag(ShdangleL)~=0 || imag(LatangleL)~=0)
       fprintf(1,'Imaginary Angle!!!!!!!!\n')
       break
   end
    
    channel=1;              %initial channel
    no=3;                   %number of channels (in chain) to control
    device = 12;
    
    ShdBLpulseL=round(5700+ShdangleL*2000/90) ;
    KneBLpulseL=round(7000-KneangleL*5000/90);
    LatBLpulseL=round(6100+LatangleL*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseL, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseL, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseL, 8:14))), '[^\w'']', ''));
       
    fopen(ser1);
    
    command1 = [170, device, 31, no, channel, lower1, upper1, lower2, upper2, lower3, upper3]; % Modification for multiple servos
    fwrite(ser1, command1);
    
    channel 
    lower1
    upper1
    lower2
    upper2
    lower3 
    upper3
    
    % Right back leg command
    
    ShdBLpulseR=round(5500-ShdangleR*2000/90); %Config for back right leg
    KneBLpulseR=round(5800+KneangleR*2000/90);
    LatBLpulseR=round(6100-LatangleR*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseR, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseR, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseR, 8:14))), '[^\w'']', ''));
    
    command = [170, device, 31, 3, 16, lower1, upper1, lower2, upper2, lower3, upper3];
    
    16
    lower1 
    upper1 
    lower2 
    upper2 
    lower3 
    upper3
    
    fwrite(ser1, command);
    fclose(ser1);
    
end

shoulder=[40 20 -10];
knee=[50 30 10];
for (k=1:numel(shoulder))
    
   AngL = left_angles([shoulder(k) 0 knee(k)]); %prompt user for angles
   AngR = right_angles([shoulder(k) 0 knee(k)]);

   %=========OUTPUT OF THE CALCULATED ANGLES==========
   
   ShdangleL=AngL(1);
   LatangleL=AngL(2); 
   KneangleL=AngL(3);         %prints the required angle of the lateral servo
   
   ShdangleR=AngR(1);
   LatangleR=AngR(2);
   KneangleR=AngR(3);
   
   if (imag(ShdangleL)~=0 || imag(LatangleL)~=0)
       fprintf(1,'Imaginary Angle!!!!!!!!\n')
       break
   end
    
    fopen(ser1);
    
    channel=1;              %initial channel
    no=3;                   %number of channels (in chain) to control
    device = 12;
    
    ShdBLpulseL=round(5700+ShdangleL*2000/90) ;
    KneBLpulseL=round(7000-KneangleL*5000/90);
    LatBLpulseL=round(6100+LatangleL*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseL, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseL, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseL, 8:14))), '[^\w'']', ''));
    
    command = [170, device, 31, no, channel, lower1, upper1, lower2, upper2, lower3, upper3]; % Modification for multiple servos
    fwrite(ser1, command);
    
    %Right back leg command
    ShdBLpulseR=round(5500-ShdangleR*2000/90); %Config for back right leg
    KneBLpulseR=round(5800+KneangleR*2000/90);
    LatBLpulseR=round(6100-LatangleR*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseR, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseR, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseR, 8:14))), '[^\w'']', ''));
    command = [170, device, 31, 3, 16, lower1, upper1, lower2, upper2, lower3, upper3];
    
    fwrite(ser1, command);
    fclose(ser1);   
end

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

knee=[10 20 30 50 80];
for (i=1:numel(knee))
    
    AngL = left_angles([-10 0 knee(i)]); %prompt user for angles
    AngR = right_angles([-10 0 knee(i)]);
   %=========OUTPUT OF THE CALCULATED ANGLES==========
   
   ShdangleL=AngL(1);
   LatangleL=AngL(2);
   KneangleL=AngL(3) ;        %prints the required angle of the lateral servo
   
   ShdangleR=AngR(1);
   LatangleR=AngR(2);
   KneangleR=AngR(3);
   
   if (imag(ShdangleL)~=0 || imag(LatangleL)~=0)
       fprintf(1,'Imaginary Angle!!!!!!!!\n')
       break
   end
    
    channel=12;              %initial channel
    no=3;                   %number of channels (in chain) to control
    device = 12;
    
    ShdBLpulseL=round(6000+ShdangleL*2000/90) ;
    KneBLpulseL=round(7000-KneangleL*5000/90);
    LatBLpulseL=round(6100+LatangleL*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseL, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseL, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseL, 8:14))), '[^\w'']', ''));
    
    command1 = [170, device, 31, 4, channel, lower1, upper1, lower2, upper2, lower3, upper3,lower3, upper3]; % Modification for multiple servos
    fopen(ser1);
    
    % Send the command
    fwrite(ser1, command1);
    
    %Right front leg command
    ShdBLpulseR=round(4700-ShdangleR*2000/90); %Config for front right leg
    KneBLpulseR=round(5700+KneangleR*4500/90);
    LatBLpulseR=round(6100-LatangleR*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseR, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseR, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseR, 8:14))), '[^\w'']', ''));
    
    command = [170, device, 31, 3, 5, lower1, upper1, lower2, upper2, lower3, upper3]; % Modification for multiple servos
    
    % Send the command
    fwrite(ser1, command);
    fclose(ser1);
end

shoulder=[-10 10 20 40 50];

for (j=1:numel(shoulder))
    
   AngL =left_angles([shoulder(j) 0 knee(i)]); %prompt user for angles
   AngR = right_angles([shoulder(j) 0 knee(i)]);

   %=========OUTPUT OF THE CALCULATED ANGLES==========
  
   ShdangleL=AngL(1);
   LatangleL=AngL(2); 
   KneangleL=AngL(3);         %prints the required angle of the lateral servo
   
   ShdangleR=AngR(1);
   LatangleR=AngR(2);
   KneangleR=AngR(3);
   
   if (imag(ShdangleL)~=0 || imag(LatangleL)~=0)
       fprintf(1,'Imaginary Angle!!!!!!!!\n')
       break
   end
    
    channel=12;              %initial channel
    no=3;                   %number of channels (in chain) to control
    device = 12;
    
    ShdBLpulseL=round(6000+ShdangleL*2000/90) ;
    KneBLpulseL=round(7000-KneangleL*5000/90);
    LatBLpulseL=round(6100+LatangleL*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseL, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseL, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseL, 8:14))), '[^\w'']', ''));
       
    fopen(ser1);
    
    command1 = [170, device, 31, 4, channel, lower1, upper1, lower2, upper2, lower3, upper3, lower3, upper3]; % Modification for multiple servos
    fwrite(ser1, command1);
    
    % Right back leg command
    
    ShdBLpulseR=round(4700-ShdangleR*2000/90); %Config for back right leg
    KneBLpulseR=round(5700+KneangleR*4500/90);
    LatBLpulseR=round(6100-LatangleR*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseR, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseR, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseR, 8:14))), '[^\w'']', ''));
    
    command = [170, device, 31, 3, 5, lower1, upper1, lower2, upper2, lower3, upper3];
    fwrite(ser1, command);
    fclose(ser1);
    
end

shoulder=[40 20 -10];
knee=[50 30 10];
for (k=1:numel(shoulder))
    
   AngL = left_angles([shoulder(k) 0 knee(k)]); %prompt user for angles
   AngR = right_angles([shoulder(k) 0 knee(k)]);

   %=========OUTPUT OF THE CALCULATED ANGLES==========
   
   ShdangleL=AngL(1);
   LatangleL=AngL(2); 
   KneangleL=AngL(3);         %prints the required angle of the lateral servo
   
   ShdangleR=AngR(1);
   LatangleR=AngR(2);
   KneangleR=AngR(3);
   
   if (imag(ShdangleL)~=0 || imag(LatangleL)~=0)
       fprintf(1,'Imaginary Angle!!!!!!!!\n')
       break
   end
    
    fopen(ser1);
    
    channel=12;              %initial channel
    no=3;                   %number of channels (in chain) to control
    device = 12;
    
    ShdBLpulseL=round(6000+ShdangleL*2000/90) ;
    KneBLpulseL=round(7000-KneangleL*5000/90);
    LatBLpulseL=round(6100+LatangleL*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseL, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseL, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseL, 8:14))), '[^\w'']', ''));
    
    command = [170, device, 31, 4, channel, lower1, upper1, lower2, upper2, lower3, upper3,lower3, upper3]; % Modification for multiple servos
    fwrite(ser1, command);
    
    %Right back leg command
    ShdBLpulseR=round(4700-ShdangleR*2000/90); %Config for back right leg
    KneBLpulseR=round(5700+KneangleR*4500/90);
    LatBLpulseR=round(6100-LatangleR*2000/90);
    
    lower1 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper1 = bin2dec(regexprep(mat2str(fliplr(bitget(ShdBLpulseR, 8:14))), '[^\w'']', ''));
    lower2 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper2 = bin2dec(regexprep(mat2str(fliplr(bitget(KneBLpulseR, 8:14))), '[^\w'']', ''));
    lower3 = bin2dec(regexprep(mat2str(fliplr(bitget(6120, 1:7))), '[^\w'']', ''));
    upper3 = bin2dec(regexprep(mat2str(fliplr(bitget(LatBLpulseR, 8:14))), '[^\w'']', ''));
    command = [170, device, 31, 3, 5, lower1, upper1, lower2, upper2, lower3, upper3];
    
    fwrite(ser1, command);
    fclose(ser1);   
end
end
delete(ser1);
 