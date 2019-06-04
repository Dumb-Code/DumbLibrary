#!/bin/sh

setup_git() {
  
  git clone https://github.com/Dumb-Code/Changelogs.git
  
  cd Changelogs
  
  git remote rm origin
  git remote add origin https://Travis-CI:${GH_TOKEN}@github.com/Dumb-Code/Changelogs.git
  
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "Travis CI"
  git config --global commit.gpgsign false
 
}

edit_file() {
  echo "$COMMITTER_NAME ($COMMITTER_EMAIL) @ $COMMIT_HASH" >> dumb_library.txt
  echo "    $TRAVIS_COMMIT_MESSAGE" >> dumb_library.txt
  echo "" >> dumb_library.txt
}

push_files() {
  git add dumb_library.txt
  git commit --message "Dumb Library Update: $TRAVIS_BUILD_NUMBER:$COMMIT_HASH"
  git push origin master
}

setup_git
edit_file
push_files