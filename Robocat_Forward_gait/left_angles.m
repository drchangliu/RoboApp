%----------------------------------------------------------------------------------
%Ryan Lucas 
%10/16/2015
%Robocat project 
%RRSSR parallel robot IPK
%----------------------------------------------------------------------------------
function leg_angls = left_angles(ang_in)

DR = pi/180;            %Conversion factor from degrees to radians
R1 = 26.2642;           %[r1] (mm)
R2 = 56.47;           %[r2] (mm)
R3 = [-4.480 21.9371];   %[r3y r3xz] (mm)
R4 = [-40 -30 -64.45]; %[r4x r4y r4z] (mm)

r1 = R1;
r2 = R2;
r3y = R3(1); r3xz = R3(2);
r4x = R4(1); r4y = R4(2); r4z = R4(3);
r3 = sqrt(r3y^2 + r3xz^2);              %compute the magnitude of r3
r4 = sqrt(r4x^2 + r4y^2 + r4z^2);        %compute the magnitude of r4

Ang = ang_in;
%phi3 is the actual angle of the leg
%th2 is the angle of the thigh in the sagital plane
th2 = -Ang(1)*DR; phi3 = Ang(2)*DR;
    
    phi1 = phi3 - 27*DR;  %phi1 corresponds to the vector r1
    %r1 is the vector from the passive universal shoulder joint to the
    %center of the spherical joint on the thigh (see diagram and CAD model)

    %position analysis
    E = 2*r4z*r3 - 2*r3*r1*sin(phi1);
    F = 2*r4x*r3 - 2*r3*r1*cos(phi1)*sin(th2);
    G = r4^2 + r3^2 + r1^2 -r2^2 - 2*r4x*r1*cos(phi1)*sin(th2) + 2*r4y*r3y + 2*r4y*r1*cos(phi1)*cos(th2) + 2*r3y*r1*cos(phi1)*cos(th2) - 2*r4z*r1*sin(phi1);
    %G = r4^2 + r3^2 + r1^2 -r2^2 - 2*r4x*r1*cos(phi1)*sin(th2) + 2*r4y*r3y
    %+ 2*r4y*r1*cos(phi1)*cos(th2) + 2*r3y*r1*cos(phi1)*cos(th2) -
    %2*r4z*r1*sin(phi1);
    p=[G-E, 2*F, G+E];
    t = (-F + sqrt(E^2 + F^2 - G^2)) / (G-E); % solve t for the open branch
    % t is calculated using the quadratic formula
    % t is then used to solve for the desired th3 value
    % if you want the other solution to th3, just switch the sign of the
    % quadratic
    
    th3 = 2*atan(t);

 leg_angls=[ang_in(1), th3/DR, ang_in(3)];