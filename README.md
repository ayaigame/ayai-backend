ayai-backend
============

This repository contains the Java and Scala source files for the backend components of the AyAI server.
These components are:

<ul>
<li>AI</li>
<li>Networking</li>
<li>Game World Management / Entity System</li>
</ul>

THIS PROJECT DOES NOT IMPORT WELL INTO ECLIPSE, USE A TOOL LIKE "SUBLIME TEXT" OR "VIM" TO EDIT.

These instructions are only to run the server.  To run the frontend code, look at the read me here:

https://github.com/ayaigame/ayai-frontend

Instructions for installing

Windows Installation
====================

First you need to install sbt, git and Java 7

http://www.scala-sbt.org/0.13.2/docs/Getting-Started/Setup.html

Download the SBT Windows MSI executable

You can download git gui and command line tools from 

http://git-scm.com/download/win

Download latest java version here

https://www.java.com/en/download/

RUNNING IN WINDOWS
==================

After these are installed you now have to clone the ayai-backend and ayai-frontend repositories

using git gui or git command line, in the directory you want, run :

"git clone https://github.com/ayaigame/ayai-backend.git"

to go to a new branch (it is not good to change code in master/develop) do:

"git branch <new-branch>"

then to run the server do

"sbt run" 


Mac
===


First you need to install sbt, git and Java 7

In the command line, If running Macports do :

"port install sbt"

If running homebrew do:

"brew install sbt"

Run the commands shown on the git page here to install git:

http://git-scm.com/book/en/Getting-Started-Installing-Git


Download latest java version here

https://www.java.com/en/download/
=================================

RUNNING IN WINDOWS
==================

After these are installed you now have to clone the ayai-backend and ayai-frontend repositories

using git gui or git command line, in the directory you want, run :

"git clone https://github.com/ayaigame/ayai-backend.git"

to go to a new branch (it is not good to change code in master/develop) do:

"git branch <new-branch>"

then to run the server do

"sbt run" 


RUNNING IN MAC
==============

Same instructions as windows
