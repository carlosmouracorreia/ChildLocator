# Child Locator - Network Security Master's Project


## Project Structure 

-> backend folder contains Server Side API, implemented in pure PHP
	-->backend/protected folder contains all the protected actions
		-->backend/protected/Keys contains Cipher KEYS. Read below for more info

-> android folder contains Client APP with gradle structure, optimized to one click build/run with Android Studio >= 1.3 

-> database.sql contains full database structure, as well as the create database query

-> Virtualhosts_apache.conf  is the Apache Configuration File for the domain used within this project 
	--- Some SECURE server policies defined here!! (For example, the protected folder world access)


-> childlocator-unsigned.apk is the final APK application, that can be used in Any Android Cellphone with >= 4.0 Version
	ATTENTION: Unknown Sources must be Enabled in Phone Settings / Security in order to run Apps external to Google Play Store




## Cryptographic information  (Example)

backend/protected/Keys contain all the keys used by the API

-> client_api_certificate.pem is the Certificate containing Private Key for decipher of android encrypted messages 
	---> ANDROID file AppConfig.java contains the PublicKey, for client side encryption 

-> DBCertificate.pem is the Certificate containing Private Key for decipher Database content (Latitudes and Longitudes)
	---> DBPublic.pem contains the PublicKey, for API encryption of incoming coordinates