package com.nafiux.grails.classdomainuml

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

class ClassDomainUMLController {

    def index() {
        /*
            package foo2 <<Rect>> {
              class Class2
            }
            println model.getShortName()
            println model.getPackageName()
            println model.getFullName()
        */
        def packages = [:]
        def relations = []
        // Generamos lista de paquetes y clases que lo contienen
        for (model in grailsApplication.domainClasses) {
            if (!packages[model.getPackageName()]) packages[model.getPackageName()] = []
            packages[model.getPackageName()].add(model)
        }
        String uml = ""
        // Iteramos paquetes
        for (p in packages) {
            uml += "package " + p.getKey() + " <<Rect>> {\n"
            // Iteramos sobre las clases de cada paquete
            for(model in p.getValue()) {
                def c = grailsApplication.classLoader.loadClass("${model.fullName}")
                def instance = new DefaultGrailsDomainClass(c)
                uml += "class " + model.getFullName() + " {\n" // Class start
                // Properties
                instance.getProperties().each {
                    uml += " " + it.getName() + ": " + it.getType().toString().replaceAll("class ", "") + "\n"
                }
                // Associations
                instance.getAssociations().each {
                    def left = "", right = "", type = "o--"
                    if(it.isManyToOne()) {
                        left = '"*"'
                        right = '"1"'
                    } else if(it.isOneToMany()) {
                        left = '"1"'
                        right = '"*"'
                    } else if(it.isOneToOne()) {
                        left = '"1"'
                        right = '"1"'
                    } else if(it.isManyToMany()) {
                        left = '"*"'
                        right = '"*"'
                    } else if(it.isEmbedded()) {
                        type = "*--"
                    }
                    relations.add(model.getFullName() + ' ' + left + ' ' + type + ' ' + right +' ' + it.getType().name + " : " + it.getName())
                }
                uml += "}\n" // Class end
            }
            uml += "}\n" // Package end
        }
        for(r in relations) {
            uml += r + "\n"
        }
        
        uml += """
title ${grailsApplication.metadata.'app.name'} - ${grailsApplication.metadata.'app.version'}
legend left
  Powered by Nafiux (nafiux.com)
  <b>Author: </b>Ignacio Ocampo
  Grails version: Grails: ${grailsApplication.metadata.'app.grails.version'}
endlegend
"""
        
        //print uml
        
        //render "<img src=""http://www.plantuml.com/plantuml/img" + compressAndEncodeString("Q->R") + " />";
        //render "Vamos bien: " + ClassDomainUMLService.testMethod()
        
        render "<img src='http://www.plantuml.com/plantuml/img/${compressAndEncodeString(uml)}' />"
        
    }
    
    def compressAndEncodeString(String str) {
        byte[] xmlBytes = str.getBytes("UTF-8");

        //net.sourceforge.plantuml.code.CompressionZlib compress = new net.sourceforge.plantuml.code.CompressionZlib()
        com.nafiux.grails.classdomainuml.CompressionZlib compress = new com.nafiux.grails.classdomainuml.CompressionZlib()
        byte[] compressed = compress.compress(xmlBytes)

        //net.sourceforge.plantuml.code.AsciiEncoder ascii = new net.sourceforge.plantuml.code.AsciiEncoder()
        com.nafiux.grails.classdomainuml.AsciiEncoder ascii = new com.nafiux.grails.classdomainuml.AsciiEncoder()

        return ascii.encode(compressed)
    }

}
