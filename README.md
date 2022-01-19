[![Build App](https://github.com/zskelton/CarWarmupTimer/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/zskelton/CarWarmupTimer/actions/workflows/gradle-publish.yml)

Car Warmup Timer v1.0 
======
<img align="left" width="100" height="100" alt="logo" src="./webassets/icon.png?raw=true">

Recently, the weather got cold and my wife complained that my son was taking off too soon in the car.
To help him realize when it is a good time to let the car warm up and for how long, I built this app.

<br /><br />
<img align="right" width="164" height="369" alt="lightmode" src="./webassets/phone_lightmode.png?raw=true">
<br />

## Features:
- [x] Look up Current Weather
- [x] Calculate Time Based on Temperature
- [x] Run Timer to Allow Warmup
- [x] Notify User when Complete

## Usage:
* Open Application, it will request location services on first use.
  * Without location services, temperature will always show as Iowa
* Hit Refresh if Needing more time.
* Hit Start to begin timer.
* Wait for timer to complete, notification will send.
<img align="right" width="164" height="369" alt="darkmode" src="./webassets/phone_darkmode.png?raw=true">

## Times:
* Temperatures above 32°F will set timer to 10 seconds.
* Temperatures of 21°F to 32°F will set timer to 100 seconds.
* Temperatures of 11°F to 20°F will set timer to 200 seconds.
* Temperatures below 10°F will set timer to 300 seconds.

## Interface:
* Refresh Button - Checks temperature for location and sets time.
* Start/Reset Button - Starts the timer (will do a quick temp check first)
* Mom Mode Switch - Because the times are pretty long.[^note]

## Purpose:
I designed and created this app as an exercise to learn Android Studio and build an app using Kotlin.
The app itself is not necessarily a need, but it was a good method to exercise some coding for Android Development!

[^note]: Mom Mode exists because my wife believes cars need to warm up for a LONG TIME.
As the kid drives a gasoline vehicle, warmup beyond 30 seconds to a 1 minute are just for comfort while driving.
