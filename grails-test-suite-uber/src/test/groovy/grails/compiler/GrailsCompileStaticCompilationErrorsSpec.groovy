package grails.compiler
import grails.persistence.Entity

import org.codehaus.groovy.control.MultipleCompilationErrorsException

import spock.lang.Ignore
import spock.lang.Issue
import spock.lang.Specification


class GrailsCompileStaticCompilationErrorsSpec extends Specification {

    @Issue('GRAILS-11056')
    void 'Test compiling valid dynamic finder calls'() {
        given:
        def gcl = new GroovyClassLoader()

        when: 'a class marked with @GrailsCompileStatic invokes valid dynamic finders'
        def c = gcl.parseClass('''
package grails.compiler

@GrailsCompileStatic
class SomeClass {

    def someMethod() {
        List<Person> people = Person.findAllByName('William')
        people = Person.listOrderByName('William')
        int number = Person.countByName('William')
        Person person = Person.findByName('William')
        person = Person.findOrCreateByName('William')
        person = Person.findOrSaveByName('William')
    }
}
''')
        then: 'no errors are thrown'
        c
    }

    @Ignore    
    @Issue(['GRAILS-11056', 'GRAILS-11057'])
    void 'Test comping a dynmaic finder call with the wrong number of arguments'() {
        given:
        def gcl = new GroovyClassLoader()
        
        when: 'a class marked with @GrailsCompileStatic invokes a dynamic finder with the wrong number of arguments'
        gcl.parseClass('''
package grails.compiler

@GrailsCompileStatic
class SomeClass {
    def someMethod() {
        Person.findAllByName('Hugh', 'Howey')
    }
}''')
        
        then: 'an error is thrown'
        MultipleCompilationErrorsException e = thrown()
        e.message.contains 'Cannot find matching method java.lang.Class#findAllByName'

    }

    @Issue('GRAILS-11056')
    void 'Test compiling invalid dynamic finder calls'() {
        given:
        def gcl = new GroovyClassLoader()

        when: 'a class marked with @GrailsCompileStatic invokes dynamic finders on a non-domain class'
        def c = gcl.parseClass('''
package grails.compiler

@GrailsCompileStatic
class SomeClass {

    def someMethod() {
        List<SomeClass> people = SomeClass.findAllByName('William')
        people = SomeClass.listOrderByName('William')
        int number = SomeClass.countByName('William')
        SomeClass person = SomeClass.findByName('William')
        person = SomeClass.findOrCreateByName('William')
        person = SomeClass.findOrSaveByName('William')
    }
}
''')
        then: 'errors are thrown'
        MultipleCompilationErrorsException e = thrown()
        e.message.contains 'Cannot find matching method java.lang.Class#findAllByName'
        e.message.contains 'Cannot find matching method java.lang.Class#listOrderByName'
        e.message.contains 'Cannot find matching method java.lang.Class#countByName'
        e.message.contains 'Cannot find matching method java.lang.Class#findByName'
        e.message.contains 'Cannot find matching method java.lang.Class#findOrCreateByName'
        e.message.contains 'Cannot find matching method java.lang.Class#findOrSaveByName'
    }

    @Issue('GRAILS-11056')
    void 'Test compiling invalid dynamic finder calls in a method marked with TypeCheckingMode.SKIP'() {
        given:
        def gcl = new GroovyClassLoader()

        when: 'a class marked with @GrailsCompileStatic invokes dynamic finders on a non-domain class inside of a method marked with TypeCheckingMode.SKIP'
        def c = gcl.parseClass('''
package grails.compiler

import groovy.transform.TypeCheckingMode

@GrailsCompileStatic
class SomeClass {

    @GrailsCompileStatic(TypeCheckingMode.SKIP)
    def someMethod() {
        List<SomeClass> people = SomeClass.findAllByName('William')
        people = SomeClass.listOrderByName('William')
        int number = SomeClass.countByName('William')
        SomeClass person = SomeClass.findByName('William')
        person = SomeClass.findOrCreateByName('William')
        person = SomeClass.findOrSaveByName('William')
    }
}
''')
        then: 'no errors are thrown'
        c
    }
}

@Entity
class Person {
    String name
}
