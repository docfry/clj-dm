VERSION=3.1.6
echo "Creating DMG for version $VERSION"

JAR="director-musices-3.1.6.alpha2-standalone-openjdk17.jar"

jpackage --input target/ \
  --name DirectorMusices \
  --main-jar $JAR \
  --main-class director_musices.main \
  --type dmg \
  --app-version $VERSION \
  --vendor "Anders Friberg" \
  --copyright "Copyright 2024" \
  --verbose \
  --java-options '--enable-preview'