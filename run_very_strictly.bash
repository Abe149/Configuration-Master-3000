#!/usr/bin/env bash

if [ "$0" = sh -o "$0" = /bin/sh -o "$0" = /usr/bin/sh -o "$0" = bash -o "$0" = -bash -o "$0" = /bin/bash -o "$0" = /usr/bin/bash ]; then
  echo 'Please do _not_ source this file!'
  return 1
fi

echo
for countdown in {9..1}; do
  echo Are you sure you want to run the server in very-strict mode?
  echo Press Control-C [or do something else similar to that] if not...  counting down, $countdown ...
  echo
  sleep 1s
done

pushd $(dirname `readlink -e "$0"`) > /dev/null
ant && java -jar ./Build/jars/Configuration_Master.jar verbosity=9 "$@" strictness_level=2 # "strictness_level=2" is _after_ “"$@"” here on _purpose_, to ensure that this script will operate like it says in its filename
result="$?"
/usr/bin/env echo -e "\nResult code: $result"
popd > /dev/null
exit $result
