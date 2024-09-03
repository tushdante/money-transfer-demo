source ../setcloudenv.sh
ENCRYPT_PAYLOADS=$1 ./gradlew run --console=plain

echo "Navigate to http://localhost:7070/"
