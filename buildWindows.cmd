set JAVA_HOME=%~dp0..\..\Jdk7x86
set MAVEN_HOME=%~dp0..\..\Maven
set PATH=%MAVEN_HOME%\bin;%JAVA_HOME%\bin;%PATH%
cd /d %~dp0
call mvn.cmd install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
ping 127.0.0.1 -n 2 > NUL
echo D | xcopy /y target\Homespawn-?.?.?.jar %~dp0..\..\Server\plugins\Homespawn.jar
pause
