#!/usr/bin/env bash

# Go to the main function for the entrypoint

if [[ "$OSTYPE" == "darwin"* ]]; then
  SED=(sed -i "")
else
  SED=(sed -i)
fi


WARN_COLOR='\033[1;33m'
INFO_COLOR='\033[1;32m'
DEFAULT_COLOR='\033[0m'

info() {
  msg=$1
  echo -e "${INFO_COLOR}${msg}${DEFAULT_COLOR}"
}

warn() {
  msg=$1
  echo -e "${WARN_COLOR}${msg}${DEFAULT_COLOR}"
}

gather_settings() {
  DEFAULT_PROJECT_NAME=${PWD##*/}
  VALID_PROJECT_NAME_REGEX='^[a-z][a-z0-9\-]*[a-z0-9]$'
  read -p "Please enter your project name (lower-case, without spaces, dashes allowed, default: $DEFAULT_PROJECT_NAME): " PROJECT_NAME
  PROJECT_NAME="${PROJECT_NAME:-DEFAULT_PROJECT_NAME}"
  
  if [[ ! $PROJECT_NAME =~ $VALID_PROJECT_NAME_REGEX ]];
  then
     warn "Project name '$PROJECT_NAME' is not valid (expecting: $VALID_PROJECT_NAME_REGEX)"
     warn "Continuing but there is a risk that the delivery pipeline will fail"
     warn "Press Ctrl+S/Cmd+S if you want to stop"
  fi

  read -p "Please enter your project description: " PROJECT_DESCRIPTION

  DEFAULT_NAMESPACE="com.pttrn42.microprimer.servicechassispringboot"
  read -p "Please enter your project namespace, $DEFAULT_NAMESPACE to be replaced with: " NAMESPACE
  NAMESPACE="${NAMESPACE:-$DEFAULT_NAMESPACE}"

  DEFAULT_PACKAGE_NAME=${PROJECT_NAME/-/.}
  DEFAULT_PACKAGE_NAME=${DEFAULT_PACKAGE_NAME//[^[:alnum:].]/}
  read -p "Please enter your project subpackage, $NAMESPACE will be prepended (default: $DEFAULT_PACKAGE_NAME): " PACKAGE_NAME
  PACKAGE_NAME="${PACKAGE_NAME:-$DEFAULT_PACKAGE_NAME}"

  read -p "Please enter your team name from GitHub: " GITHUB_TEAM_NAME
  info "================================================================="
  info "Project name: $PROJECT_NAME"
  info "Description: $PROJECT_DESCRIPTION"
  info "Package: $NAMESPACE.$PACKAGE_NAME"
  info "Github CODEOWNERS: $GITHUB_TEAM_NAME"
  info "================================================================="

}

setup_team() {
  echo "Setting team"
  "${SED[@]}" -e "s/kubamarchwicki/$GITHUB_TEAM_NAME/g" .github/CODEOWNERS
}

setup_description() {
  echo "Setting description"
  DESCRIPTION_PLACEHOLDER="This service provides an API for ..."
  "${SED[@]}" -e "s/$DESCRIPTION_PLACEHOLDER/$PROJECT_DESCRIPTION/g" README_template.md
  "${SED[@]}" -e "s/$DESCRIPTION_PLACEHOLDER/$PROJECT_DESCRIPTION/g" app/src/main/java/com/pttrn42/microprimer/servicechassispringboot/infrastructure/api/SwaggerConfiguration.java
  "${SED[@]}" -e "s/$DESCRIPTION_PLACEHOLDER/$PROJECT_DESCRIPTION/g" app/src/main/resources/static/index.html
}

setup_project_name() {
  echo "Setting project name"
  PROJECT_NAME_PLACEHOLDER="service-chassis-springboot"
  "${SED[@]}" -e "s/$DEFAULT_NAMESPACE/$NAMESPACE/g" pom.xml
  "${SED[@]}" -e "s/$DEFAULT_NAMESPACE/$NAMESPACE/g" app/pom.xml
  "${SED[@]}" -e "s/$DEFAULT_NAMESPACE/$NAMESPACE/g" blackboxtests/pom.xml
  "${SED[@]}" -e "s/$PROJECT_NAME_PLACEHOLDER/$PROJECT_NAME/g" pom.xml
  "${SED[@]}" -e "s/$PROJECT_NAME_PLACEHOLDER/$PROJECT_NAME/g" app/pom.xml
  "${SED[@]}" -e "s/$PROJECT_NAME_PLACEHOLDER/$PROJECT_NAME/g" blackboxtests/pom.xml
  "${SED[@]}" -e "s/$PROJECT_NAME_PLACEHOLDER/$PROJECT_NAME/g" app/src/main/resources/application.yml
  "${SED[@]}" -e "s/$PROJECT_NAME_PLACEHOLDER/$PROJECT_NAME/g" app/src/main/java/com/pttrn42/microprimer/servicechassispringboot/infrastructure/api/SwaggerConfiguration.java
  "${SED[@]}" -e "s/$PROJECT_NAME_PLACEHOLDER/$PROJECT_NAME/g" app/src/main/resources/static/index.html
  "${SED[@]}" -e "s/$PROJECT_NAME_PLACEHOLDER/$PROJECT_NAME/g" blackboxtests/src/test/java/com/pttrn42/microprimer/servicechassispring/SmokeTest.java
  "${SED[@]}" -e "s/$PROJECT_NAME_PLACEHOLDER/$PROJECT_NAME/g" README_template.md
}

move_sources() {
  SOURCE_SET=$1
  mkdir -p $SOURCE_SET/java/${NAMESPACE//\.//}/$PACKAGE_DIRECTORY
  mv $SOURCE_SET/java/com/pttrn42/microprimer/servicechassispringboot/* $SOURCE_SET/java/${NAMESPACE//\.//}/$PACKAGE_DIRECTORY
  rm -rf $SOURCE_SET/java/com/pttrn42/microprimer/servicechassispringboot
}

setup_package() {
  echo "Setting project package"
  PACKAGE_DIRECTORY=${PACKAGE_NAME//\./\/}
  for SOURCE_SET in app/src/* blackboxtests/src/*; do
    echo "Setting up $SOURCE_SET source set"
    find $SOURCE_SET -type f -name '*.java' -exec "${SED[@]}" -e "s/$DEFAULT_NAMESPACE/$NAMESPACE.$PACKAGE_NAME/g" {} +
    move_sources $SOURCE_SET
  done
}

cleanup() {
  echo "Cleaning up"
  rm README.adoc
  mv README_template.md README.md
  rm -rf setup.sh
  git add .
}

do_setup() {
  setup_project_name
  setup_description
  setup_package
  setup_team
  cleanup
  info "======================================="
  info "Project $PROJECT_NAME setup complete!"
  info "Please review changes"
  info "======================================="
}

main () {
dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)
cd $dir

if [ "`git status -s`" ]
then
    echo "The working directory is dirty. Please commit any pending changes."
    exit 1;
fi

gather_settings
read -p "Shall we begin? (y/N): " confirm
case "$confirm" in
y | Y) do_setup ;;
*)
  warn "Aborting!"
  exit 1
  ;;
esac
}

main
