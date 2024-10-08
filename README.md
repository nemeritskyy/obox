# OpenAPI Swagger
http://localhost:8080/obox/swagger-ui/index.html  
>Now there is a setting that after restarting the server your user data will be deleted  
If you need use already created user please use  
login: admin@mail.com  
password: password  
# POSTMAN
[My workspace for fast application testing](https://www.postman.com/orbital-module-participant-50713643/workspace/obox/request/28060010-cc530140-600d-4a66-a686-daa7d908d6c1)  
![N|Solid](https://nemeritskyy.pp.ua/obox/24.png)  
# Tools for local using project on your PC
## _Recommend to use directories and password's as tutorial_
## _Install Java JDK:_
For Windows x64 you can use fast link:  
https://download.oracle.com/java/19/archive/jdk-19.0.2_windows-x64_bin.exe  
For other:  
https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html  

Run installer with Administrator rights  
Select destination folder, I recommend create folder for backend tools, for example: C:\obox\java  
![N|Solid](https://nemeritskyy.pp.ua/obox/0.png)

## _Download Apache Tomcat Server with link:_
32-bit/64-bit Windows Service Installer:  
[TomCat 9.0.76 32-bit/64-bit Windows Service Installer](https://dlcdn.apache.org/tomcat/tomcat-9/v9.0.76/bin/apache-tomcat-9.0.76.exe)  

1. Run installer with Administrator rights  
2. Select all Checkboxes including "Host Manager", "Example"  
![N|Solid](https://nemeritskyy.pp.ua/obox/1.png)
3. We must specify the password and the user for example user:obox / password:obox  
![N|Solid](https://nemeritskyy.pp.ua/obox/1_1.png)
3. Select your Java directory for our example: C:\obox\java  
![N|Solid](https://nemeritskyy.pp.ua/obox/2.png)
4. Folder for Tomcat C:\obox\tomcat\  
5. Run Apache Tomcat, you can see icon in tray  
![N|Solid](https://nemeritskyy.pp.ua/obox/3.png)

> For test open in your browser: http://localhost:8080  
> My congratulations, we are halfway to success!  
> Add war file from repository to directory C:\obox\tomcat\webapps and open your browser http://localhost:8080/obox/

## _Install MySQL_
## _In Future after reboot you PC start first MySQL Workbench, after that Tomcat_

Fast link Windows (x86, 32-bit), MSI Installer:  
https://dev.mysql.com/get/Downloads/MySQLInstaller/mysql-installer-web-community-8.0.33.0.msi  
Step-by-step installation as in the images  
![N|Solid](https://nemeritskyy.pp.ua/obox/6.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/7.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/8.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/9.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/10.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/11.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/12.png)  
Set and remember root password for example: oboxroot  
![N|Solid](https://nemeritskyy.pp.ua/obox/13.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/14.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/15.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/16.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/17.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/18.png)  
Use our root password in our example: oboxroot  
![N|Solid](https://nemeritskyy.pp.ua/obox/19.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/20.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/21.png)  
Use our root password in our example: oboxroot  
![N|Solid](https://nemeritskyy.pp.ua/obox/22.png)
![N|Solid](https://nemeritskyy.pp.ua/obox/23.png)  
> My congratulations, database success installed!
