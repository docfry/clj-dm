VERSION=3.1.6
echo "Creating DMG for version $VERSION"

JAR="director-musices-3.1.6.alpha2-standalone-openjdk17.jar"

jpackage --input target/ \
  --name DirectorMusices \
  --main-jar $JAR \
  --main-class org.myapp.Main \
  --type dmg \
  --app-version $VERSION \
  --vendor "Your name" \
  --copyright "Copyright 2023 Your name" \
  --mac-package-name "Director Musices 3.1.6" \
  --mac-package-identifier "org.myapp" \
  --verbose \
  --java-options '--enable-preview'