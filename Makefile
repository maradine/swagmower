all:
	javac -cp lib/pircbotx-1.9.jar:lib/teamspeak-3-api-r8.jar:. *.java

clean:
	rm -rf *.class
