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
    envs.each(env -> println("Deploying to $env..."))
    // translation: for each value from envs, print the message
  }
}
