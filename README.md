# proxy_example

openssl s_client -connect your.proxy.host:443 -showcerts </dev/null | openssl x509 -outform PEM > proxy.crt






keytool -import -alias proxy-cert -file proxy.crt -keystore truststore.jks -storepass changeit


