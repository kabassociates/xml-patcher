package org.kohsuke.maven.rewrite.edit;

import com.google.common.base.Predicate;
import org.kohsuke.maven.rewrite.Mark;
import org.kohsuke.maven.rewrite.XmlPatcher;
import org.kohsuke.maven.rewrite.XmlPath;
import org.kohsuke.maven.rewrite.XmlVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * Insert a new element inside an element content model.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class InsertInElement extends XmlVisitor {
    private final Predicate<XmlPath> matcher;

    public InsertInElement(Predicate<XmlPath> matcher) {
        this.matcher = matcher;
    }

    private XmlPath lastStart;
    private final Set<XmlPath> rewritten = new HashSet<XmlPath>();

    private Mark start;

    @Override
    public void startDocument(XmlPatcher scanner) {
        super.startDocument(scanner);
        rewritten.clear();
        start = getPatcher().mark();
    }

    @Override
    public void startElement() {
        lastStart = getPath();
        XmlPath parent = getPath().getParent();
        if (matcher.apply(parent) && !rewritten.contains(parent)) {
            // insert before this element

            // this captures the indent we want to use after the element is inserted
            doInsert(parent);
        }

        start.set();
    }

    private void doInsert(XmlPath parent) {
        Mark r = start.toCurrent();

        String indent = r.verbatim();
        String inserted = insert();
        if (inserted!=null && inserted.length()!=0) {
            r.replace(indent+inserted+indent);
            r.clear();
            rewritten.add(parent);
        }
    }

    @Override
    public void endElement() {
        if (matcher.apply(getPath()) && lastStart==getPath()) {
            // if we didn't see any element inside where we wanted to rewrite, we come here
            doInsert(getPath());
        }

        start.set();
    }

    protected abstract String insert();
}
