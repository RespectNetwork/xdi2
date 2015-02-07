package xdi2.core.syntax;

import java.util.List;

public abstract class ParserAbstract implements Parser {

	protected XDIStatement newXDIStatement(String string, XDIAddress subject, XDIAddress predicate, XDIArc object) {

		return new XDIStatement(string, subject, predicate, object);
	}

	protected XDIStatement newXDIStatement(String string, XDIAddress subject, XDIAddress predicate, XDIAddress object) {

		return new XDIStatement(string, subject, predicate, object);
	}

	protected XDIStatement newXDIStatement(String string, XDIAddress subject, XDIAddress predicate, Object object) {

		return new XDIStatement(string, subject, predicate, object);
	}
	
	protected XDIAddress newXDIAddress(String string, List<XDIArc> arcs) {

		return new XDIAddress(string, arcs);
	}

	protected XDIArc newXDIArc(String string, Character cs, boolean variable, boolean collection, boolean attribute, String literal, XDIXRef xref) {

		return new XDIArc(string, cs, variable, collection, attribute, literal, xref);
	}

	protected XDIXRef newXDIXRef(String string, String xs, XDIAddress XDIaddress, XDIAddress partialSubject, XDIAddress partialPredicate, String iri, String literal) {

		return new XDIXRef(string, xs, XDIaddress, partialSubject, partialPredicate, iri, literal);
	}
}