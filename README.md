This project is created and customized for personal usage only. 
It is a bunch of modules, combined to work with different APIs in a concrete environment.

The main goal was to collect various systems in one place and have one API for custom mobile application.

Custom application is created using Flutter for Android. TODO link

## Main features:
- Use ELKO api to interact with the smart home - curtains, LED light, warm floor, etc.
For security purposes, direct calls to the ELKO REST api are be removed.
- Use air conditioner API to turn on/off, set temperature, mode, etc. Source code was imported to extend the implemented features. https://github.com/alexmuntean/gree-airconditioner-rest
- Use Shelly Door/Window 2 device API to detect windows opening/closing, light level, etc.
- Create custom scenarios, combining features above.
- Use custom nodes, which provide temperature and humidity values.
- Use qbittorent API to download files, provide storage info, send notifications in Telegram
- Use Telegram API for notifications, statistic reports
- Create heat map of home
- Use sockets to notify connected application about changes
- Collect working time (electricity availability)

### esp32
- ESP32-WROOM-32D and DHT22 are used for temperature/humidity nodes
- Code is developed and deployed using Arduino IDE

### home-info-starter
- Separate service **home-info-starter** is used to start main server on os startup. Also, the API provided by this server
  is used to upgrade and restart main server. Upgrade steps are - download latest code from git, maven clean install, start

### audio
- The purpose was to create audio message using google voice generation api. api is free for limited time, so currently
the feature is not working

### Torrent
1. download and install torrent client https://www.qbittorrent.org/ (API https://github.com/qbittorrent/qBittorrent/wiki/WebUI-API-(qBittorrent-4.1))
2. enable WebUI API
3. change port to 8092
4. select no login from localhost in settings
5. enable start up with Windows