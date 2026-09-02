package com.sourcegraph.demo.bigbadmonolith.common.it;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Hashtable;

/**
 * Minimal in-memory JNDI {@link Context} that resolves exactly one name to a {@link DataSource} and
 * throws {@link NamingException} for anything else. Only {@link #lookup(String)} and {@link #close()}
 * are supported; every other {@link Context} method is intentionally unimplemented because the code
 * under test never calls them.
 */
final class SingleDataSourceContext implements Context {

    private final String boundName;
    private final DataSource dataSource;

    SingleDataSourceContext(String boundName, DataSource dataSource) {
        this.boundName = boundName;
        this.dataSource = dataSource;
    }

    @Override
    public Object lookup(String name) throws NamingException {
        if (boundName.equals(name)) {
            return dataSource;
        }
        throw new NamingException("Name not bound in test context: " + name);
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    @Override
    public void close() {
        // Nothing to release; the backing DataSource owns the in-memory database lifecycle.
    }

    private static UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Not supported by the test JNDI context");
    }

    @Override
    public void bind(Name name, Object obj) {
        throw unsupported();
    }

    @Override
    public void bind(String name, Object obj) {
        throw unsupported();
    }

    @Override
    public void rebind(Name name, Object obj) {
        throw unsupported();
    }

    @Override
    public void rebind(String name, Object obj) {
        throw unsupported();
    }

    @Override
    public void unbind(Name name) {
        throw unsupported();
    }

    @Override
    public void unbind(String name) {
        throw unsupported();
    }

    @Override
    public void rename(Name oldName, Name newName) {
        throw unsupported();
    }

    @Override
    public void rename(String oldName, String newName) {
        throw unsupported();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) {
        throw unsupported();
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) {
        throw unsupported();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) {
        throw unsupported();
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) {
        throw unsupported();
    }

    @Override
    public void destroySubcontext(Name name) {
        throw unsupported();
    }

    @Override
    public void destroySubcontext(String name) {
        throw unsupported();
    }

    @Override
    public Context createSubcontext(Name name) {
        throw unsupported();
    }

    @Override
    public Context createSubcontext(String name) {
        throw unsupported();
    }

    @Override
    public Object lookupLink(Name name) {
        throw unsupported();
    }

    @Override
    public Object lookupLink(String name) {
        throw unsupported();
    }

    @Override
    public NameParser getNameParser(Name name) {
        throw unsupported();
    }

    @Override
    public NameParser getNameParser(String name) {
        throw unsupported();
    }

    @Override
    public Name composeName(Name name, Name prefix) {
        throw unsupported();
    }

    @Override
    public String composeName(String name, String prefix) {
        throw unsupported();
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) {
        throw unsupported();
    }

    @Override
    public Object removeFromEnvironment(String propName) {
        throw unsupported();
    }

    @Override
    public Hashtable<?, ?> getEnvironment() {
        throw unsupported();
    }

    @Override
    public String getNameInNamespace() {
        throw unsupported();
    }
}
