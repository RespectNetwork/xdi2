package xdi2.tests.core.syntax;


import junit.framework.TestCase;
import xdi2.core.constants.XDIConstants;
import xdi2.core.syntax.Parser;
import xdi2.core.syntax.XDIAddress;
import xdi2.core.syntax.XDIArc;
import xdi2.core.syntax.XDIStatement;
import xdi2.core.syntax.XDIXRef;
import xdi2.core.syntax.parser.ParserException;

public abstract class ParserAbstractTest extends TestCase {

	public void testBasic() throws Exception {

		Parser parser = this.getParser();

		XDIStatement statement = parser.parseXDIStatement("=markus[<#email>]<*!1>/&/\"xxx\"");

		assertEquals(statement.getSubject(), parser.parseXDIAddress("=markus[<#email>]<*!1>"));
		assertEquals(statement.getPredicate(), parser.parseXDIArc("&"));
		assertEquals(statement.getObject(), "xxx");

		assertEquals(statement.getContextNodeXDIAddress(), parser.parseXDIAddress("=markus[<#email>]<*!1>"));
		assertNull(statement.getContextNodeXDIArc());
		assertNull(statement.getTargetXDIAddress());
		assertEquals(statement.getLiteralData(), "xxx");

		assertEquals(statement.getSubject().getNumXDIArcs(), 3);
		assertEquals(statement.getSubject().getXDIArc(0), statement.getSubject().getFirstXDIArc());
		assertEquals(statement.getSubject().getXDIArc(2), statement.getSubject().getLastXDIArc());
		assertEquals(statement.getSubject().getXDIArc(0), parser.parseXDIArc("=markus"));
		assertEquals(statement.getSubject().getXDIArc(0).getCs(), XDIConstants.CS_AUTHORITY_PERSONAL);
		assertEquals(statement.getSubject().getXDIArc(0).getLiteral(), "markus");
		assertNull(statement.getSubject().getXDIArc(0).getXRef());
		assertEquals(statement.getSubject().getXDIArc(1), parser.parseXDIArc("[<#email>]"));
		assertEquals(statement.getSubject().getXDIArc(1).getCs(), XDIConstants.CS_CLASS_UNRESERVED);
		assertTrue(statement.getSubject().getXDIArc(1).isCollection());
		assertTrue(statement.getSubject().getXDIArc(1).isAttribute());
		assertEquals(statement.getSubject().getXDIArc(1).getLiteral(), "email");
		assertNull(statement.getSubject().getXDIArc(1).getXRef());
		assertEquals(statement.getSubject().getXDIArc(2), parser.parseXDIArc("<*!1>"));
		assertEquals(statement.getSubject().getXDIArc(2).getCs(), XDIConstants.CS_INSTANCE_UNORDERED);
		assertFalse(statement.getSubject().getXDIArc(2).isCollection());
		assertTrue(statement.getSubject().getXDIArc(2).isAttribute());
		assertEquals(statement.getSubject().getXDIArc(2).getLiteral(), "1");
		assertNull(statement.getSubject().getXDIArc(2).getXRef());

		assertTrue(statement.getPredicate() instanceof XDIArc);
		assertEquals(statement.getPredicate(), XDIConstants.XDI_ARC_LITERAL);
	}

	public void testBasicXRef() throws Exception {

		Parser parser = this.getParser();

		XDIAddress XDIaddress = parser.parseXDIAddress("+(user)<#(first_name)>");

		assertEquals(XDIaddress.getNumXDIArcs(), 2);
		assertEquals(XDIaddress.getXDIArc(0), parser.parseXDIArc("+(user)"));
		assertEquals(XDIaddress.getXDIArc(1), parser.parseXDIArc("<#(first_name)>"));
	}

	public void testXDIStatement() throws Exception {

		Parser parser = this.getParser();

		XDIStatement statement;

		statement = parser.parseXDIStatement("=markus<#email>/&/\"markus@projectdanube.org\"");
		assertEquals(statement.getSubject(), parser.parseXDIAddress("=markus<#email>"));
		assertEquals(statement.getPredicate(), parser.parseXDIArc("&"));
		assertEquals(statement.getPredicate(), XDIConstants.XDI_ARC_LITERAL);
		assertTrue(statement.getObject() instanceof String);
		assertEquals(statement.getObject(), "markus@projectdanube.org");
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		statement = parser.parseXDIStatement("=markus/+friend/=neustar*animesh");
		assertEquals(statement.getSubject(), parser.parseXDIAddress("=markus"));
		assertEquals(statement.getPredicate(), parser.parseXDIAddress("+friend"));
		assertEquals(statement.getObject(), parser.parseXDIAddress("=neustar*animesh"));
		assertFalse(statement.isContextNodeStatement());
		assertFalse(statement.isLiteralStatement());
		assertTrue(statement.isRelationStatement());

		statement = parser.parseXDIStatement("=neustar*animesh<#email>/&/\"animesh@gmail.com\"");
		assertEquals(statement.getSubject(), parser.parseXDIAddress("=neustar*animesh<#email>"));
		assertEquals(statement.getPredicate(), parser.parseXDIAddress("&"));
		assertEquals(statement.getObject(), "animesh@gmail.com");
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		statement = parser.parseXDIStatement("=neustar*animesh<#age>/&/99");
		assertEquals(statement.getSubject(), parser.parseXDIAddress("=neustar*animesh<#age>"));
		assertEquals(statement.getPredicate(), parser.parseXDIAddress("&"));
		assertEquals(statement.getObject(), Double.valueOf(99));
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		statement = parser.parseXDIStatement("=neustar*animesh<#smoker>/&/false");
		assertEquals(statement.getSubject(), parser.parseXDIAddress("=neustar*animesh<#smoker>"));
		assertEquals(statement.getPredicate(), parser.parseXDIAddress("&"));
		assertEquals(statement.getObject(), Boolean.valueOf(false));
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		statement = parser.parseXDIStatement("=neustar*animesh<#color>/&/null");
		assertEquals(statement.getSubject(), parser.parseXDIAddress("=neustar*animesh<#color>"));
		assertEquals(statement.getPredicate(), parser.parseXDIAddress("&"));
		assertNull(statement.getObject());
		assertFalse(statement.isContextNodeStatement());
		assertTrue(statement.isLiteralStatement());
		assertFalse(statement.isRelationStatement());

		try {

			statement = parser.parseXDIStatement("=neustar*animesh<#err>/&/test");
			fail();
		} catch (ParserException ex) {

		}

		statement = parser.parseXDIStatement("=neustar*animesh/+friend/=markus");
		assertEquals(statement.getSubject(), parser.parseXDIAddress("=neustar*animesh"));
		assertEquals(statement.getPredicate(), parser.parseXDIAddress("+friend"));
		assertEquals(statement.getObject(), parser.parseXDIAddress("=markus"));
		assertFalse(statement.isContextNodeStatement());
		assertFalse(statement.isLiteralStatement());
		assertTrue(statement.isRelationStatement());
	}

	public void testXDIXRef() throws Exception {

		Parser parser = this.getParser();

		XDIXRef xref;

		xref = parser.parseXDIXRef("()");
		assertTrue(xref.isEmpty());

		xref = parser.parseXDIXRef("(=markus)");
		assertFalse(xref.isEmpty());
		assertEquals(parser.parseXDIArc("=markus"), xref.getXDIArc());

		xref = parser.parseXDIXRef("(=markus/$add)");
		assertFalse(xref.isEmpty());
		assertEquals(parser.parseXDIAddress("=markus"), xref.getPartialSubject());
		assertEquals(parser.parseXDIAddress("$add"), xref.getPartialPredicate());

		xref = parser.parseXDIXRef("(data:,markus@projectdanube.org)");
		assertFalse(xref.isEmpty());
		assertEquals("data:,markus@projectdanube.org", xref.getIri());

		xref = parser.parseXDIXRef("(email)");
		assertFalse(xref.isEmpty());
		assertEquals("email", xref.getLiteralNode());
	}

	public void testLiteralXRef() {

		Parser parser = this.getParser();

		XDIArc s;

		s = parser.parseXDIArc("({[<#(name)>]})");
		assertTrue(s.hasXRef());
		assertEquals(s.getXRef(), parser.parseXDIXRef("({[<#(name)>]})"));
		assertEquals(s.getXRef().getXs(), XDIConstants.XS_ROOT);
		assertTrue(s.getXRef().hasXDIArc());
		assertEquals(s.getXRef().getXDIArc(), parser.parseXDIArc("{[<#(name)>]}"));
		assertEquals(s.getXRef().getXDIArc(), parser.parseXDIArc("{[<#(name)>]}"));
		assertTrue(s.getXRef().getXDIArc().isCollection());
		assertTrue(s.getXRef().getXDIArc().isAttribute());
		assertTrue(s.getXRef().getXDIArc().hasXRef());
		assertEquals(s.getXRef().getXDIArc().getXRef(), parser.parseXDIXRef("(name)"));
		assertEquals(s.getXRef().getXDIArc().getXRef().getXs(), XDIConstants.XS_ROOT);
		assertTrue(s.getXRef().getXDIArc().getXRef().hasLiteralNode());
		assertEquals(s.getXRef().getXDIArc().getXRef().getLiteralNode(), "name");
	}

	public void testComponents() throws Exception {

		XDIStatement contextNodeStatement = XDIStatement.create("=markus//[<#email>]");
		XDIStatement contextNodeStatement2 = XDIStatement.fromComponents(XDIAddress.create("=markus"), XDIConstants.STRING_CONTEXT, XDIArc.create("[<#email>]"));
		XDIStatement contextNodeStatement3 = XDIStatement.fromContextNodeComponents(XDIAddress.create("=markus"), XDIArc.create("[<#email>]"));

		assertEquals(contextNodeStatement.getSubject(), XDIAddress.create("=markus"));
		assertEquals(contextNodeStatement.getPredicate(), XDIConstants.STRING_CONTEXT);
		assertEquals(contextNodeStatement.getObject(), XDIArc.create("[<#email>]"));

		assertEquals(contextNodeStatement, contextNodeStatement2);
		assertEquals(contextNodeStatement, contextNodeStatement3);

		XDIStatement relationStatement = XDIStatement.create("=markus/+friend/=animesh");
		XDIStatement relationStatement2 = XDIStatement.fromComponents(XDIAddress.create("=markus"), XDIAddress.create("+friend"), XDIAddress.create("=animesh"));
		XDIStatement relationStatement3 = XDIStatement.fromRelationComponents(XDIAddress.create("=markus"), XDIAddress.create("+friend"), XDIAddress.create("=animesh"));

		assertEquals(relationStatement, relationStatement2);
		assertEquals(relationStatement, relationStatement3);

		assertEquals(relationStatement.getSubject(), XDIAddress.create("=markus"));
		assertEquals(relationStatement.getPredicate(), XDIAddress.create("+friend"));
		assertEquals(relationStatement.getObject(), XDIAddress.create("=animesh"));

		XDIStatement literalStatement = XDIStatement.create("=markus<#name>/&/\"Markus Sabadello\"");
		XDIStatement literalStatement2 = XDIStatement.fromComponents(XDIAddress.create("=markus<#name>"), XDIConstants.XDI_ARC_LITERAL, "Markus Sabadello");
		XDIStatement literalStatement3 = XDIStatement.fromLiteralComponents(XDIAddress.create("=markus<#name>"), "Markus Sabadello");

		assertEquals(literalStatement.getSubject(), XDIAddress.create("=markus<#name>"));
		assertEquals(literalStatement.getPredicate(), XDIConstants.XDI_ARC_LITERAL.toString());
		assertEquals(literalStatement.getObject(), "Markus Sabadello");

		assertEquals(literalStatement, literalStatement2);
		assertEquals(literalStatement, literalStatement3);
	}

	public abstract Parser getParser();
}
