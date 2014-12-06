=====================================================================
SAME - Sistema de Gestion de Turnos por Internet <http://same.sagant.com>
The source code of SAME can be downloaded from <https://github.com/sagant/same>
SAME is a fork of SAE - Sistema de Agenda Electronica <http://www.montevideo.gub.uy/institucional/montevideo-abierto/software-libre>
 
Copyright (C) 2013, 2014  SAGANT - Codestra S.R.L. <http://www.sagant.com/>
Copyright (C) 2013, 2014  Alvaro Rettich <alvaro@sagant.com>
Copyright (C) 2013, 2014  Carlos Gutierrez <carlos@sagant.com>
Copyright (C) 2013, 2014  Victor Dumas <victor@sagant.com>

This file is part of SAME.

SAME is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
=====================================================================


This document is about howto install SAME in a fresh Jboss AS 7.1.1.Final instance.
Este documento describe como instalar SAME en una instancia nueva de Jboss AS 7.1.1.Final.
Son necesarios 3 pasos:
(1)setup jboss para SAME, 
(2)Compilar y empaquear la aplicacion SAME, 
(3)Desplegar la app.
Cuando la aplicacion este levantda y ejecutándose, la misma puede ser accedida de dos formas:
Backend (login required): https://localhost:8443/same-admin
Frontend: https://localhost:8443/same/agendarReserva/Paso1.xhtml?agenda=xxx&recurso=yyy
where xxx is the group name of previusly created Group and 
yyy the Agenda name of previusly created Agenda

Cuando se requiere login, utilizar el usuario "admin" contraseña "admin"

----------------------------
0) Requicitos previos
----------------------------

Para la instalación y ejecución de la aplicación es necesario tener instalado el siguiente software:

	- Oracle/Sun JDK 1.7 u OpenJDK 1.7
	- Apache ANT (http://ant.apache.org/)
	- Apache Maven 3
	- JBoss AS versión 7.1.1.Final (http://jboss.org/jbossas/)
	- Base de Datos: PostgreSQL 9.3 o superior (http://www.postgresql.org/)
	- Drivers JDBC4 para la base de datos: (http://jdbc.postgresql.org/download.html)
	- La IDE de desarrollo utilizada es Eclipse Juno + plugins de git y maven
	- Openssl si va a crear su propio certificado para la conexion https
 
NOTA1: La expresión <JBOSS_HOME> refiere al directorio donde se encuentra instalado el servidor de aplicaciones.
NOTA2: La expresión <SAME_HOME> refiere al directorio base del repositorio git donde se encuentra el codigo fuente de SAME.

-------------------------------
1) Configuración del Servidor 
-------------------------------


1.1 Driver JDBC
-------------------------------
Crear un modulo jboss para almacenar el driver, para esto:

	1- Crear la carpeta <JBOSS_HOME>/modules/org/postgresql/jdbc/main
	2- cp <SAME_HOME>/install/libs/postgresql-9.2-1002.jdbc4.jar <JBOSS_HOME>/modules/org/postgresql/jdbc/main
	3- Crear el achivo <JBOSS_HOME>/modules/org/postgresql/jdbc/main/module.xml con el siguiente contenido:

<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.1" name="org.postgresql.jdbc">
	<resources>
		<resource-root path="postgresql-9.2-1002.jdbc4.jar"/>
	</resources>
	<dependencies>
		<module name="javax.api"/>
		<module name="javax.transaction.api"/>
	</dependencies>
</module>


1.2 Certificado SSL
-------------------------------
El login de la aplicación utiliza https para el envío de las credenciales del usuario. 
Para su correcto funcionamiento, se debe habilitar el uso de SSL en el servidor.
Dicha habilitacion sera realizada por el script de configuracion, sin embargo sera
necesario crear el certificado SSL e instalarlo en el servidor de forma manual.

A continuación se muestran los comandos básicos para generar un almacen de claves
que contenga un certificado autofirmado. 
Dicho certificado autofirmado es a los solos efectos de poder probar la aplicación, 
bajo ningun concepto debe ser utilizado en un ambiente de producción.

	cd <JBOSS_HOME>;
	mkdir certs;
	cd certs;

	#ejemplo de pass phrase: changeit
	openssl genrsa -des3 -out jboss.key;

	#pass phrase: changeit
	#El resto de los datos llenarlos según lo que corresponda
	openssl req -new -x509 -key jboss.key -out jboss.crt

	#pass phrase: changeit
	#export password: changeit
	openssl pkcs12 -inkey jboss.key -in jboss.crt -export -out jboss.pkcs12

	#contraseña almacen de claves destino: changeit
	#contraseña almacen de claves origen: changeit
	keytool -importkeystore -srckeystore jboss.pkcs12 -srcstoretype PKCS12 -destkeystore .keystore

	mv .keystore ../standalone/configuration/


1.3 Crear la base de datos
-------------------------------
Crear la base de datos de nombre "same" 


1.4 Configurar Usuarios/Roles para desarrollo
-------------------------------
En la configuración automatizada del Jboss se utilizará un sistema de usuarios y roles 
basado en archivo de propiedades, apto solamente para ámbientes de prueba o desarrollo.
Para el cual será necesario copiar los archivos de propiedades de ejemplo y modificarlos según el caso.

cp <SAME_HOME>/install/same*.properties <JBOSS_HOME>/standalone/configuration


1.5 Configurar el Jboss
-------------------------------
Asegurarse que el jboss sea ejecutado usando la jvm 1.7 ya sea de Oracle u OpenJdk
Para esto editar el <JBOSS_HOME>/bin/standalone.conf
y agregar la linea:

	JAVA_HOME="/usr/lib/jvm/java-7-openjdk-amd64/"

	o
	
	JAVA_HOME="/usr/lib/jvm/java-7-oracle"

según corresponda.

Levantar el jboss ejecutando <JBOSS_HOME>/bin/standalone.sh

Hay varias tareas de configuración automatizadas en el script setup-jboss-as-7.1.cli
Para lo cual hay que ejecutar:

	cd <SAME_HOME>/install
	<JBOSS_HOME>/bin/jboss-cli.sh --file=setup-jboss-7.1.cli
	


-------------------------------
2) Compilar y empaquetar la app
-------------------------------
Asegurarse que el java seteado en el entrono sea 1.7 (java -version) y 
la misma implementación con la que ejecuta su instalacion de jboss. 
si no: 
	export JAVA_HOME="/usr/lib/jvm/java-7-openjdk-amd64/"

	o
	
	export JAVA_HOME="/usr/lib/jvm/java-7-oracle"

según corresponda.

cd <SAME_HOME>/sae
mvn clean package

-------------------------------
3) Despliegue de la app
-------------------------------

cp <SAME_HOME>/deployments/* <JBOSS_HOME>/standalone/deployments 



-------------------------------
4) REFERENCIAS
-------------------------------

Documentacion sobre como configurar un Jboss AS 7.1.1.Final utiliznado CLI

Para saber como usar CLI ver: https://community.jboss.org/wiki/CommandLineInterface

CLI se basa en el AS7 detyped management model. 
Para AS71 ver: https://docs.jboss.org/author/display/AS71/Description+of+the+Management+Model 
Para AS72 ver: https://docs.jboss.org/author/display/AS72/Description+of+the+Management+Model


:)

