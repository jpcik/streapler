package rsp.rspql.parser;

import java.io.Reader;
import java.io.StringReader;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.lang.SPARQLParser;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;

public class ParserRspql extends SPARQLParser {

	private interface Action {
		void exec(RspqlParser parser) throws Exception;
	}

	@Override
	public Query parse$(final Query query, String queryString) {
		query.setSyntax(Syntax.syntaxSPARQL);

		Action action = new Action() {
			public void exec(RspqlParser parser) throws Exception {
				parser.Query();
			}
		};

		perform(query, queryString, action);
		validateParsedQuery(query);
		return query;
	}

	public static Element parseElement(String string) {
		final Query query = new Query();
		Action action = new Action() {
			public void exec(RspqlParser parser) throws Exception {
				Element el = parser.GroupGraphPattern();
				query.setQueryPattern(el);
			}
		};
		perform(query, string, action);
		return query.getQueryPattern();
	}

	public static Template parseTemplate(String string) {
		final Query query = new Query();
		Action action = new Action() {
			public void exec(RspqlParser parser) throws Exception {
				Template t = parser.ConstructTemplate();
				query.setConstructTemplate(t);
			}
		};
		perform(query, string, action);
		return query.getConstructTemplate();
	}

	// All throwable handling.
	private static void perform(Query query, String string, Action action) {
		Reader in = new StringReader(string);
		RspqlParser parser = new RspqlParser(in);

		try {
			query.setStrict(true);
			parser.setQuery(query);
			action.exec(parser);
		} catch (ParseException ex) {
			throw new QueryParseException(ex.getMessage(),
					ex.currentToken.beginLine, ex.currentToken.beginColumn);
		} catch (TokenMgrError tErr) {
			// Last valid token : not the same as token error message - but this
			// should not happen
			int col = parser.token.endColumn;
			int line = parser.token.endLine;
			throw new QueryParseException(tErr.getMessage(), line, col);
		}

		catch (QueryException ex) {
			throw ex;
		} catch (JenaException ex) {
			throw new QueryException(ex.getMessage(), ex);
		} catch (Error err) {
			// The token stream can throw errors.
			throw new QueryParseException(err.getMessage(), err, -1, -1);
		}
		catch (Throwable th) {
			// ALog.warn(ParserSPARQLstr.class, "Unexpected throwable: ",th) ;
			throw new QueryException(th.getMessage(), th);
		}
	}
}