#!/bin/sh
# Maven Wrapper Script for CosmosDomain
# Falls back to system Maven if wrapper cannot be downloaded

# Try system Maven first
if [ -x "$(command -v mvn)" ]; then
  exec mvn "$@"
fi

# Fallback: try common locations
for maven_dir in "/usr/share/maven" "/opt/maven" "$HOME/maven" "/home/geomastro/maven"; do
  if [ -x "$maven_dir/bin/mvn" ]; then
    export PATH="$maven_dir/bin:$PATH"
    exec mvn "$@"
  fi
done

echo "Maven not found. Please install Maven or ensure it's in your PATH."
exit 1