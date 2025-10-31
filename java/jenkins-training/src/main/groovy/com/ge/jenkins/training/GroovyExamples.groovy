package com.ge.jenkins.training

class GroovyExamples {

  // psvm = public static void main
  static void main(String[] args) {
    def hello = "Hello, Groovy!" // def = creating a variable
    println hello

    def port = 22
    print port

    println() // thank you, Mihai

    println "Using interpolation - $port"

    def envs = ["dev", "qa", "pre"] //array of values

    // functional
    envs.each(env -> println("Deploying to $env..."))

    // imperative
    for (env in envs) {
      println("Deploying to $env...")
    }
    // translation: for each value from envs, print the message

    if (port == 22) {
      print "Do something"
    }

    println "env variable value ${System.properties.JAVA_HOME}"
  }
}
