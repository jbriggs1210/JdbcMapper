package com.moparisthebest.jdbc.codegen;

import com.moparisthebest.jdbc.Cleaner;
import com.moparisthebest.jdbc.MapperException;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.*;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//IFJAVA8_START
import java.time.*;
//IFJAVA8_END

import static com.moparisthebest.jdbc.TryClose.tryClose;

/**
 * Created by mopar on 5/24/17.
 */
@SupportedAnnotationTypes("com.moparisthebest.jdbc.codegen.JdbcMapper.Mapper")
@SupportedOptions({"jdbcMapper.databaseType", "jdbcMapper.arrayNumberTypeName", "jdbcMapper.arrayStringTypeName", "JdbcMapper.allowedMaxRowParamNames"})
@SupportedSourceVersion(SourceVersion.RELEASE_5)
public class JdbcMapperProcessor extends AbstractProcessor {

	private static final Pattern paramPattern = Pattern.compile("\\{(([^\\s]+)\\s+(([Nn][Oo][Tt]\\s+)?[Ii][Nn]\\s+))?([^}]+)\\}");

	public static final SourceVersion RELEASE_8;
	public static boolean java8;

	static {
		SourceVersion rl8 = null;
		try {
			rl8 = SourceVersion.valueOf("RELEASE_8");
		} catch(Throwable e) {
			// ignore
		}
		RELEASE_8 = rl8;
	}

	private static Types types;
	private static Messager messager;

	public static Types getTypes() {
		return types;
	}

	public static Messager getMessager() {
		return messager;
	}

	private TypeMirror sqlExceptionType, stringType, numberType, utilDateType, readerType, clobType, jdbcMapperType,
			byteArrayType, inputStreamType, fileType, blobType, sqlArrayType, collectionType, calendarType, cleanerType;
	//IFJAVA8_START
	private TypeMirror instantType, localDateTimeType, localDateType, localTimeType, zonedDateTimeType, offsetDateTimeType, offsetTimeType;
	//IFJAVA8_END
	private TypeElement cleanerElement;
	private JdbcMapper.DatabaseType defaultDatabaseType;
	private String defaultArrayNumberTypeName, defaultArrayStringTypeName;
	private Set<String> allowedMaxRowParamNames;
	private CompileTimeResultSetMapper rsm;

	public JdbcMapperProcessor() {
		//out.println("JdbcMapperProcessor running!");
	}

	@Override
	public synchronized void init(final ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		// is this the proper way to do this?
		java8 = RELEASE_8 != null && processingEnv.getSourceVersion().ordinal() >= RELEASE_8.ordinal();

		types = processingEnv.getTypeUtils();
		messager = processingEnv.getMessager();
		final Elements elements = processingEnv.getElementUtils();
		sqlExceptionType = elements.getTypeElement(SQLException.class.getCanonicalName()).asType();
		stringType = elements.getTypeElement(String.class.getCanonicalName()).asType();
		numberType = elements.getTypeElement(Number.class.getCanonicalName()).asType();
		utilDateType = elements.getTypeElement(java.util.Date.class.getCanonicalName()).asType();
		readerType = elements.getTypeElement(Reader.class.getCanonicalName()).asType();
		clobType = elements.getTypeElement(Clob.class.getCanonicalName()).asType();
		jdbcMapperType = elements.getTypeElement(JdbcMapper.class.getCanonicalName()).asType();
		inputStreamType = elements.getTypeElement(InputStream.class.getCanonicalName()).asType();
		fileType = elements.getTypeElement(File.class.getCanonicalName()).asType();
		blobType = elements.getTypeElement(Blob.class.getCanonicalName()).asType();
		calendarType = elements.getTypeElement(Calendar.class.getCanonicalName()).asType();
		//IFJAVA8_START
		instantType = elements.getTypeElement(Instant.class.getCanonicalName()).asType();
		localDateTimeType = elements.getTypeElement(LocalDateTime.class.getCanonicalName()).asType();
		localDateType = elements.getTypeElement(LocalDate.class.getCanonicalName()).asType();
		localTimeType = elements.getTypeElement(LocalTime.class.getCanonicalName()).asType();
		zonedDateTimeType = elements.getTypeElement(ZonedDateTime.class.getCanonicalName()).asType();
		offsetDateTimeType = elements.getTypeElement(OffsetDateTime.class.getCanonicalName()).asType();
		offsetTimeType = elements.getTypeElement(OffsetTime.class.getCanonicalName()).asType();
		//IFJAVA8_END
		// throws NPE:
		//byteArrayType = elements.getTypeElement(byte[].class.getCanonicalName()).asType();
		//byteArrayType = this.types.getArrayType(elements.getTypeElement(byte.class.getCanonicalName()).asType());
		//byteArrayType = elements.getTypeElement(byte.class.getCanonicalName()).asType();
		byteArrayType = types.getArrayType(types.getPrimitiveType(TypeKind.BYTE));
		sqlArrayType = elements.getTypeElement(java.sql.Array.class.getCanonicalName()).asType();
		collectionType = types.getDeclaredType(elements.getTypeElement(Collection.class.getCanonicalName()), types.getWildcardType(null, null));

		cleanerElement = elements.getTypeElement(Cleaner.class.getCanonicalName());
		cleanerType = types.getDeclaredType(cleanerElement, types.getWildcardType(null, null));

		final String databaseType = processingEnv.getOptions().get("JdbcMapper.databaseType");
		defaultDatabaseType = databaseType == null ? JdbcMapper.DatabaseType.STANDARD : JdbcMapper.DatabaseType.valueOf(databaseType);
		defaultArrayNumberTypeName = processingEnv.getOptions().get("JdbcMapper.arrayNumberTypeName");
		if (defaultArrayNumberTypeName == null || defaultArrayNumberTypeName.isEmpty())
			defaultArrayNumberTypeName = defaultDatabaseType.arrayNumberTypeName;
		defaultArrayStringTypeName = processingEnv.getOptions().get("JdbcMapper.arrayStringTypeName");
		if (defaultArrayStringTypeName == null || defaultArrayStringTypeName.isEmpty())
			defaultArrayStringTypeName = defaultDatabaseType.arrayStringTypeName;

		String allowedMaxRowParamNames = processingEnv.getOptions().get("JdbcMapper.allowedMaxRowParamNames");
		if (allowedMaxRowParamNames == null || allowedMaxRowParamNames.isEmpty())
			allowedMaxRowParamNames = "maxRows,rowLimit,arrayMaxLength";
		this.allowedMaxRowParamNames = new HashSet<String>(Arrays.asList(allowedMaxRowParamNames.split(",")));

		rsm = new CompileTimeResultSetMapper(processingEnv);
	}

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		//if(true) return false;
		if (annotations.isEmpty() || roundEnv.processingOver()) {
			// write out test classes
			return false;
		}
		if (isInitialized())
			//System.out.println("annotations: " + annotations);
			//System.out.println("roundEnv: " + roundEnv);
			for (final Element element : roundEnv.getElementsAnnotatedWith(JdbcMapper.Mapper.class))
				try {
					if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.INTERFACE && !element.getModifiers().contains(Modifier.ABSTRACT)) {
						processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.Mapper can only annotate an interface or abstract class", element);
						continue;
					}
					final TypeElement genClass = (TypeElement) element;
					final JdbcMapper.Mapper mapper = genClass.getAnnotation(JdbcMapper.Mapper.class);
					final JdbcMapper.DatabaseType databaseType;
					final String arrayNumberTypeName, arrayStringTypeName;
					if (mapper.databaseType() == JdbcMapper.DatabaseType.DEFAULT) {
						databaseType = defaultDatabaseType;
						arrayNumberTypeName = !mapper.arrayNumberTypeName().isEmpty() ? mapper.arrayNumberTypeName() : defaultArrayNumberTypeName;
						arrayStringTypeName = !mapper.arrayStringTypeName().isEmpty() ? mapper.arrayStringTypeName() : defaultArrayStringTypeName;
					} else {
						databaseType = mapper.databaseType();
						arrayNumberTypeName = !mapper.arrayNumberTypeName().isEmpty() ? mapper.arrayNumberTypeName() :
								(mapper.databaseType() == defaultDatabaseType ? defaultArrayNumberTypeName : mapper.databaseType().arrayNumberTypeName);
						arrayStringTypeName = !mapper.arrayStringTypeName().isEmpty() ? mapper.arrayStringTypeName() :
								(mapper.databaseType() == defaultDatabaseType ? defaultArrayStringTypeName : mapper.databaseType().arrayStringTypeName);
					}
					final String sqlParserMirror = getSqlParser(mapper).toString();
					//final SQLParser parser = new SimpleSQLParser();//(SQLParser)Class.forName(mapper.sqlParser().getCanonicalName()).newInstance();
					//final SQLParser parser = mapper.sqlParser().equals(SQLParser.class) ? new SimpleSQLParser() : mapper.sqlParser().newInstance();
					final SQLParser parser = sqlParserMirror.equals("com.moparisthebest.jdbc.codegen.SQLParser") ?
							new SimpleSQLParser() : (SQLParser) Class.forName(sqlParserMirror).newInstance();
					final String qualifiedName = genClass.getQualifiedName().toString();
					final boolean isInterface = genClass.getKind().isInterface();
					final boolean doJndi = !mapper.jndiName().isEmpty();
					Writer w = null;
					try {
						w = processingEnv.getFiler().createSourceFile(qualifiedName + JdbcMapperFactory.SUFFIX).openWriter();
						final String packageName = ((PackageElement) genClass.getEnclosingElement()).getQualifiedName().toString();
						final String className = genClass.getSimpleName() + JdbcMapperFactory.SUFFIX;
						if (!packageName.isEmpty()) {
							w.write("package ");
							w.write(packageName);
							w.write(";\n\n");
						}
						w.write("import com.moparisthebest.jdbc.Factory;\n\n");
						w.write("import java.sql.*;\n\n");
						w.write("import static com.moparisthebest.jdbc.util.ResultSetUtil.*;\n");
						w.write("import static com.moparisthebest.jdbc.TryClose.tryClose;\n\n");
						w.write("public class ");
						w.write(className);
						if (isInterface) {
							w.write(" implements ");
						} else {
							w.write(" extends ");
						}
						w.write(genClass.getSimpleName().toString());
						w.write(" {\n\n\tprivate final Connection conn;\n\tprivate final boolean closeConn;\n\n");
						if (doJndi) {
							w.write("\tprivate static final Factory<Connection> _conFactory = com.moparisthebest.jdbc.codegen.JdbcMapperFactory.connectionFactory(\"");
							w.append(mapper.jndiName()).append("\");\n\n\tpublic ");
							w.write(className);
							w.write("() throws SQLException {\n\t\tthis(_conFactory);\n\t}\n");
						}
						w.write("\n\tpublic ");
						w.write(className);
						w.write("(Connection conn) {\n\t\tthis.conn = conn;\n\t\tthis.closeConn = false;\n\t\tif (this.conn == null)\n" +
								"\t\t\tthrow new NullPointerException(\"Connection needs to be non-null for JdbcMapper...\");\n\t}\n" +
								"\n\tpublic Connection getConnection() {\n\t\treturn this.conn;\n\t}\n"
						);

						// loop through methods
						int cachedPreparedStatements = 0;
						ExecutableElement closeMethod = null;
						boolean lookupCloseMethod = true;
						final boolean defaultCachePreparedStatements;
						switch (mapper.cachePreparedStatements()) {
							case TRUE:
								defaultCachePreparedStatements = true;
								break;
							case FALSE:
								defaultCachePreparedStatements = false;
								break;
							default:
								defaultCachePreparedStatements = (closeMethod = getCloseMethod(genClass)) != null;
								lookupCloseMethod = false;
						}

						final boolean defaultAllowReflection = mapper.allowReflection().combine(false);
						final ReflectionFields reflectionFields = new ReflectionFields();

						for (final Element methodElement : genClass.getEnclosedElements()) {
							// can only implement abstract methods
							if (methodElement.getKind() != ElementKind.METHOD) {
								continue;
							}
							if (!methodElement.getModifiers().contains(Modifier.ABSTRACT)) {
								if (methodElement.getAnnotation(JdbcMapper.RunInTransaction.class) != null)
									outputRunInTransaction((ExecutableElement) methodElement, w);
								continue;
							}
							final ExecutableElement eeMethod = (ExecutableElement) methodElement;
							if (lookupCloseMethod)
								if ((closeMethod = getCloseMethod(eeMethod)) != null) {
									lookupCloseMethod = false;
									continue; // skip close method
								}
							final JdbcMapper.WarnOnUnusedParams warnOnUnusedParams = eeMethod.getAnnotation(JdbcMapper.WarnOnUnusedParams.class);
							final JdbcMapper.SQL sql = eeMethod.getAnnotation(JdbcMapper.SQL.class);
							if (sql == null || sql.value().isEmpty()) {
								processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.SQL with non-empty query is required on abstract or interface methods", methodElement);
								continue;
							}
							if (eeMethod.getAnnotation(JdbcMapper.RunInTransaction.class) != null) {
								processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.SQL is incompatible with @JdbcMapper.RunInTransaction", methodElement);
								continue;
							}
							final boolean allowReflection = sql.allowReflection().combine(defaultAllowReflection);
							w.write("\n\t@Override\n\tpublic ");
							final String returnType = eeMethod.getReturnType().toString();
							w.write(returnType);
							w.write(" ");
							w.write(eeMethod.getSimpleName().toString());
							w.write('(');

							// build query and bind param order
							final List<VariableElement> bindParams = new ArrayList<VariableElement>();
							final String sqlStatement;
							String calendarName = null, cleanerName = null;
							CompileTimeResultSetMapper.MaxRows maxRows = CompileTimeResultSetMapper.MaxRows.getMaxRows(sql.maxRows());
							boolean sqlExceptionThrown = false;
							{
								// now parameters
								final List<? extends VariableElement> params = eeMethod.getParameters();
								final Map<String, VariableElement> paramMap = new HashMap<String, VariableElement>(params.size());
								final int numParams = params.size();
								int count = 0;
								for (final VariableElement param : params) {
									writeAllParamAnnotations(w, param);
									w.write("final ");
									w.write(param.asType().toString());
									w.write(' ');
									final String name = param.getSimpleName().toString();
									w.write(name);
									paramMap.put(name, param);
									if (++count != numParams)
										w.write(", ");
								}
								final Map<String, VariableElement> unusedParams = new HashMap<String, VariableElement>(paramMap);

								// throws?
								w.write(")");
								final List<? extends TypeMirror> thrownTypes = eeMethod.getThrownTypes();
								final int numThrownTypes = thrownTypes.size();
								if (numThrownTypes > 0) {
									count = 0;
									w.write(" throws ");
								}
								for (final TypeMirror thrownType : thrownTypes) {
									sqlExceptionThrown |= types.isSameType(thrownType, sqlExceptionType);
									w.write(thrownType.toString());
									if (++count != numThrownTypes)
										w.write(", ");
								}
								w.write(" {\n");

								final Matcher bindParamMatcher = paramPattern.matcher(sql.value());
								final StringBuffer sb = new StringBuffer();
								while (bindParamMatcher.find()) {
									final String paramName = bindParamMatcher.group(5);
									final VariableElement bindParam = paramMap.get(paramName);
									if (bindParam == null) {
										processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format("@JdbcMapper.SQL sql has bind param '%s' not in method parameter list", paramName), methodElement);
										continue;
									}
									unusedParams.remove(paramName);
									final String inColumnName = bindParamMatcher.group(2);
									if (inColumnName == null) {
										bindParams.add(bindParam);
										bindParamMatcher.appendReplacement(sb, "?");
									} else {
										bindParams.add(new InListVariableElement(bindParam));
										final boolean not = bindParamMatcher.group(4) != null;
										final String replacement;
										switch (databaseType) {
											case ORACLE:
												replacement = not ?
														"(" + inColumnName + " NOT IN(select column_value from table(?)))" :
														"(" + inColumnName + " IN(select column_value from table(?)))";
												break;
											case STANDARD:
												replacement = not ?
														"(" + inColumnName + " != ANY(?))" :
														"(" + inColumnName + " = ANY(?))";
												break;
											default:
												processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "default DatabaseType? should never happen!!", bindParam);
												return false;
										}
										bindParamMatcher.appendReplacement(sb, replacement);
									}
								}
								bindParamMatcher.appendTail(sb);
								sqlStatement = sb.toString();

								for (final Map.Entry<String, VariableElement> unusedParam : unusedParams.entrySet()) {
									// look for lone calendar object
									final TypeMirror unusedType = unusedParam.getValue().asType();
									if(types.isAssignable(unusedType, calendarType) && calendarName == null)
										calendarName = unusedParam.getKey();
									else if(types.isAssignable(unusedType, cleanerType) && cleanerName == null) {
										cleanerName = unusedParam.getKey();
										/*
										// yuck this falls apart for anything other than plain objects, might as well just let it be a compile time error?
										final TypeMirror requiredType = types.getDeclaredType(cleanerElement, types.getWildcardType(null, eeMethod.getReturnType()));
										if(types.isAssignable(unusedType, requiredType))
											cleanerName = unusedParam.getKey();
										else
											processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
													String.format("@JdbcMapper.SQL method has unused parameter '%s' of cleaner type '%s' when cleaner type required is '%s'", unusedParam.getKey(), unusedType, requiredType),
													unusedParam.getValue());
										*/
									} else if(isPrimitiveInteger(unusedType.getKind()) && maxRows == null && this.allowedMaxRowParamNames.contains(unusedParam.getKey())) {
										maxRows = CompileTimeResultSetMapper.MaxRows.getMaxRows(unusedParam.getKey(), unusedType.toString());
									} else
										processingEnv.getMessager().printMessage(warnOnUnusedParams != null ? Diagnostic.Kind.MANDATORY_WARNING : Diagnostic.Kind.ERROR, String.format("@JdbcMapper.SQL method has unused parameter '%s'", unusedParam.getKey()), unusedParam.getValue());
								}
							}

							final SQLParser parsedSQl = ManualSQLParser.getSQLParser(sql, parser, sqlStatement);
							// now implementation
							w.write("\t\tPreparedStatement ps = null;\n");
							if (parsedSQl.isSelect())
								w.write("\t\tResultSet rs = null;\n");
							w.write("\t\ttry {\n\t\t\tps = ");
							final boolean cachePreparedStatements = sql.cachePreparedStatement().combine(defaultCachePreparedStatements);
							if (cachePreparedStatements) {
								w.write("this.prepareStatement(");
								w.write(Integer.toString(cachedPreparedStatements));
								w.write(", ");
								++cachedPreparedStatements;
							} else {
								w.write("conn.prepareStatement(");
							}
							w.write('"');
							w.write(sqlStatement.replace("\n", "\\n").replace("\"", "\\\""));
							w.write("\");\n");

							// now bind parameters
							int count = 0;
							for (final VariableElement param : bindParams)
								setObject(w, ++count, databaseType, arrayNumberTypeName, arrayStringTypeName, param);

							boolean closeRs = true;
							if (!parsedSQl.isSelect()) {
								if (returnType.equals("void")) {
									w.write("\t\t\tps.executeUpdate();\n");
								} else if (returnType.equals("int") || returnType.equals("java.lang.Integer")) {
									w.write("\t\t\treturn ps.executeUpdate();\n");
								} else {
									processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.SQL sql other than SELECT must return either void, int, or Integer", methodElement);
									continue;
								}
							} else {
								w.write("\t\t\trs = ps.executeQuery();\n");
								final String[] keys = parsedSQl.columnNames();
								if (keys == null || keys.length < 2) {
									processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.SQL sql parsed no column names, proper SQL? Wildcard? or bad parser?", methodElement);
									continue;
								}
								for (final String key : keys)
									if ("*".equals(key)) {
										processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.SQL sql parsed a wildcard column name which is not supported", methodElement);
										return false;
									}
								closeRs = rsm.mapToResultType(w, keys, eeMethod, maxRows, calendarName, cleanerName, !cachePreparedStatements, allowReflection ? reflectionFields : null);
							}

							// close things
							if(closeRs) {
								// like normal
								// if no SQLException is thrown, we have to catch it here and wrap it with RuntimeException...
								if (!sqlExceptionThrown)
									w.write("\t\t} catch(SQLException e) {\n\t\t\tthrow new RuntimeException(e);\n");
								w.write("\t\t} finally {\n");
								if (parsedSQl.isSelect())
									w.write("\t\t\ttryClose(rs);\n");
								if (!cachePreparedStatements)
									w.write("\t\t\ttryClose(ps);\n");
							} else {
								// very annoying special handling in that if any exceptions are thrown, we have to close everything even if closeRs == false...
								w.write("\t\t} catch(Throwable e) {\n");
								if (parsedSQl.isSelect())
									w.write("\t\t\ttryClose(rs);\n");
								if (!cachePreparedStatements)
									w.write("\t\t\ttryClose(ps);\n");
								if (sqlExceptionThrown)
									w.write("\t\t\tif(e instanceof SQLException)\n\t\t\t\tthrow (SQLException)e;\n");
								w.write("\t\t\tif(e instanceof RuntimeException)\n\t\t\t\tthrow (RuntimeException)e;\n");
								w.write("\t\t\tthrow new RuntimeException(e);\n");
							}
							w.write("\t\t}\n");

							w.write("\t}\n");
						}

						// look on super classes and interfaces recursively
						if (lookupCloseMethod)
							closeMethod = getCloseMethod(genClass);

						if (closeMethod == null && (cachedPreparedStatements > 0 || doJndi)) {
							processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@Jdbc.Mapper extended classes with cachedPreparedStatements or jndiNames must have a public void close() method to override or implement, because they must be closed", genClass);
							continue;
						}

						if(!reflectionFields.isEmpty()) {
							w.append("\n\tprivate static final java.lang.reflect.Field[] _fields = new java.lang.reflect.Field[] {\n");
							for(final VariableElement ve : reflectionFields) {
								w.append("\t\tcom.moparisthebest.jdbc.util.ReflectionUtil.getAccessibleField(")
											.append(ve.getEnclosingElement().asType().toString()).append(".class, \"")
											.append(ve.getSimpleName().toString()).append("\"),\n");
							}
							w.append("\t};\n");
						}

						if (cachedPreparedStatements > 0) {
							w.write("\n\tprivate final PreparedStatement[] psCache = new PreparedStatement[");
							w.write(Integer.toString(cachedPreparedStatements));
							w.write("];\n\n\tprivate PreparedStatement prepareStatement(final int index, final String sql) throws SQLException {\n" +
									"\t\tfinal PreparedStatement ps = psCache[index];\n" +
									"\t\treturn ps == null ? (psCache[index] = conn.prepareStatement(sql)) : ps;\n" +
									"\t}\n");
						}

						// close method
						if (closeMethod != null) {
							// if cachedPreparedStatements > 0 or doJndi are true, class MUST have a close() method to override as it
							// MUST be called to clean up
							w.write("\n\t@Override\n\tpublic void close() {\n");
							if (cachedPreparedStatements > 0)
								w.write("\t\tfor(final PreparedStatement ps : psCache)\n\t\t\ttryClose(ps);\n");
							if (doJndi)
								w.write("\t\tif(closeConn)\n\t\t\ttryClose(conn);\n");
							if (closeMethod.getEnclosingElement().getKind() != ElementKind.INTERFACE && !closeMethod.getEnclosingElement().equals(genClass))
								w.write("\t\tsuper.close();\n");
							w.write("\t}\n");

							// and we can create constructors that set closeConn to true!
							w.write("\n\tpublic ");
							w.write(className);
							w.write("(final Factory<Connection> connectionFactory) throws SQLException {\n\t\tthis.conn = connectionFactory.create();\n\t\tthis.closeConn = true;\n\t\tif (this.conn == null)\n" +
									"\t\t\tthrow new NullPointerException(\"Connection needs to be non-null for JdbcMapper...\");\n\t}\n"
							);

							w.write("\n\tpublic ");
							w.write(className);
							w.write("(final String jndiName) throws SQLException {\n\t\tthis(com.moparisthebest.jdbc.codegen.JdbcMapperFactory.connectionFactory(jndiName));\n\t}\n"
							);
						}
						// end close method

						w.write("}\n");
					} finally {
						tryClose(w);
					}
				} catch (Exception e) {
					final StringWriter sw = new StringWriter();
					sw.write('\n');
					e.printStackTrace(new PrintWriter(sw));
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage() + sw.toString(), element);
					return false;
				}
		return true;
	}

	private void outputRunInTransaction(final ExecutableElement eeMethod, final Writer w) throws IOException {
		w.write("\n\t@Override\n\tpublic ");
		final String returnType = eeMethod.getReturnType().toString();
		w.write(returnType);
		w.write(" ");
		w.write(eeMethod.getSimpleName().toString());
		w.write('(');

		boolean sqlExceptionThrown = false;
		final List<? extends VariableElement> params = eeMethod.getParameters();
		final int numParams = params.size();
		{
			// now parameters
			int count = 0;
			for (final VariableElement param : params) {
				writeAllParamAnnotations(w, param);
				w.write("final ");
				w.write(param.asType().toString());
				w.write(' ');
				final String name = param.getSimpleName().toString();
				w.write(name);
				if (++count != numParams)
					w.write(", ");
			}

			// throws?
			w.write(")");
			final List<? extends TypeMirror> thrownTypes = eeMethod.getThrownTypes();
			final int numThrownTypes = thrownTypes.size();
			if (numThrownTypes > 0) {
				count = 0;
				w.write(" throws ");
			}
			for (final TypeMirror thrownType : thrownTypes) {
				sqlExceptionThrown |= types.isSameType(thrownType, sqlExceptionType);
				w.write(thrownType.toString());
				if (++count != numThrownTypes)
					w.write(", ");
			}
			w.write(" {\n");
		}

		final Element thisDao = eeMethod.getEnclosingElement();
		final boolean thisDaoImplementsJdbcMapper = types.isAssignable(thisDao.asType(), jdbcMapperType);
		final String thisDaoName = thisDao.getSimpleName().toString();

		if(!java8)
			processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.RunInTransaction cannot be used in java6 yet, java8+ only", eeMethod);

		// todo: *can* this be done in java6 without reflection or something? we need to call super, not this, which causes infinite recursion, bail for now
		if(!java8)
			w.append("\t\tfinal ").append(thisDaoName).append(" jdbcMapperGeneratedTransactionThis = this;\n");

		w.write("\t\treturn com.moparisthebest.jdbc.QueryRunner.run");
		if(thisDaoImplementsJdbcMapper)
			w.write("InTransaction(this, ");
		else
			w.write("ConnectionInTransaction(this.conn, ");

		if(!java8) {
			w.append("new com.moparisthebest.jdbc.QueryRunner.Runner<").append(thisDaoName).append(", ").append(returnType).append(">() {\n" +
					"\t\t\t@Override\n" +
					"\t\t\tpublic ").append(returnType).append(" run(").append(eeMethod.getEnclosingElement().getSimpleName()).append(" dao) throws SQLException {\n" +
					"\t\t\t\treturn jdbcMapperGeneratedTransactionThis");
		} else {

			w.append("dao -> ");
			//IFJAVA8_START
			if (eeMethod.getModifiers().contains(Modifier.DEFAULT))
				w.append(thisDaoName).append(".");
			//IFJAVA8_END
			w.append("super");
		}
		w.append(".").append(eeMethod.getSimpleName().toString()).append('(');
		int count = 0;
		for (final VariableElement param : params) {
			final String name = param.getSimpleName().toString();
			w.write(name);
			if (++count != numParams)
				w.write(", ");
		}
		w.append(')');

		if(!java8)
			w.append(";\n\t\t\t}\n" +
				"\t\t}");

		w.write(");\n");
		w.write("\t}\n");
	}

	private void setObject(final Writer w, final int index, final JdbcMapper.DatabaseType databaseType, final String arrayNumberTypeName, final String arrayStringTypeName, final VariableElement param) throws SQLException, IOException {
		String variableName = param.getSimpleName().toString();
		final TypeMirror o = param.asType();
		w.write("\t\t\t");
		String method = null;

		// special behavior
		if (param instanceof InListVariableElement) {
			final boolean collection;
			final TypeMirror componentType;
			if (o.getKind() == TypeKind.ARRAY) {
				collection = false;
				componentType = ((ArrayType) o).getComponentType();
			} else if (o.getKind() == TypeKind.DECLARED && types.isAssignable(o, collectionType)) {
				collection = true;
				final DeclaredType dt = (DeclaredType) o;
				if (dt.getTypeArguments().isEmpty()) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.SQL in list syntax requires a Collection with a generic type parameter" + o.toString(), ((InListVariableElement) param).delegate);
					return;
				}
				componentType = dt.getTypeArguments().get(0);
			} else {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.SQL in list syntax only valid on Collections or arrays" + o.toString(), ((InListVariableElement) param).delegate);
				return;
			}
			w.write("ps.setArray(");
			w.write(Integer.toString(index));
			w.write(", ");
			final String type = types.isAssignable(componentType, numberType) ? arrayNumberTypeName : arrayStringTypeName;
			switch (databaseType) {
				case ORACLE:
					w.write("conn.unwrap(oracle.jdbc.OracleConnection.class).createOracleArray(\"");

					// todo: if oracle driver is not on compile-time classpath, would need to do:
					// we could also create a fake-oracle module that just had a oracle.jdbc.OracleConnection class implementing createOracleArray()...
					/*
					private static final Class<?> oracleConnection;
					private static final Method createArray;

					static {
						Class<?> oc;
						Method ca;
						try {
							oc = Class.forName("oracle.jdbc.OracleConnection");
							ca = oc.getDeclaredMethod("createOracleArray", String.class, Object.class);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
						oracleConnection = oc;
						createArray = ca;
					}
					 */
					//w.write("(Array) createArray.invoke(conn.unwrap(oracleConnection), \"");
					break;
				case STANDARD:
					w.write("conn.createArrayOf(\"");
					break;
				default:
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "default DatabaseType? should never happen!!", ((InListVariableElement) param).delegate);
			}
			w.write(type);
			w.write("\", ");
			w.write(variableName);
			if (collection)
				w.write(".toArray()");
			w.write("));\n");
			return;
		}
		final JdbcMapper.Blob blob = param.getAnnotation(JdbcMapper.Blob.class);
		if (blob != null) {
			if (types.isAssignable(o, stringType)) {
				w.write("try {\n\t\t\t\tps.setBlob(");
				w.write(Integer.toString(index));
				w.write(", ");
				w.write(variableName);
				w.write(" == null ? null : new java.io.ByteArrayInputStream(");
				w.write(variableName);
				w.write(".getBytes(\"");
				w.write(blob.charsetName());
				w.write("\")));\n\t\t\t} catch (java.io.UnsupportedEncodingException e) {\n" +
						"\t\t\t\tthrow new SQLException(\"String to Blob UnsupportedEncodingException\", e);\n" +
						"\t\t\t}\n");
				return;
			} else if (!(types.isAssignable(o, inputStreamType) || types.isAssignable(o, blobType) || types.isAssignable(o, fileType) || types.isAssignable(o, byteArrayType))) {
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.Blob only valid for String, byte[], Blob, InputStream, and File", param);
				return;
			}
		} else {
			final JdbcMapper.Clob clob = param.getAnnotation(JdbcMapper.Clob.class);
			if (clob != null) {
				if (types.isAssignable(o, stringType)) {
					method = "Clob";
					variableName = variableName + " == null ? null : new StringReader(" + variableName + ")";
				} else if (!(types.isAssignable(o, readerType) || types.isAssignable(o, clobType))) {
					processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.Clob only valid for String, Clob, Reader", param);
					return;
				}
			}
		}
		// end special behavior

		// we are going to put most common ones up top so it should execute faster normally
		// todo: avoid string concat here
		if (method == null)
			if (o.getKind().isPrimitive() || types.isAssignable(o, stringType) || types.isAssignable(o, numberType)) {
				method = "Object";
				// java.util.Date support, put it in a Timestamp
			} else if (types.isAssignable(o, utilDateType)) {
				method = "Object";
				// might need to wrap with Timestamp
				if (types.isSameType(o, utilDateType))
					variableName = variableName + " == null ? null : new java.sql.Timestamp(" + variableName + ".getTime())";
			}
			//IFJAVA8_START
			else if (types.isAssignable(o, instantType)) {
				method = "Object";
				variableName = variableName + " == null ? null : java.sql.Timestamp.from(" + variableName + ")";
			} else if (types.isAssignable(o, localDateTimeType)) {
				method = "Object";
				variableName = variableName + " == null ? null : java.sql.Timestamp.valueOf(" + variableName + ")";
			} else if (types.isAssignable(o, localDateType)) {
				method = "Object";
				variableName = variableName + " == null ? null : java.sql.Date.valueOf(" + variableName + ")";
			} else if (types.isAssignable(o, localTimeType)) {
				method = "Object";
				variableName = variableName + " == null ? null : java.sql.Time.valueOf(" + variableName + ")";
			} else if (types.isAssignable(o, zonedDateTimeType) || types.isAssignable(o, offsetDateTimeType)) {
				method = "Object";
				variableName = variableName + " == null ? null : java.sql.Timestamp.from(" + variableName + ".toInstant())";
			} else if (types.isAssignable(o, offsetTimeType)) {
				method = "Object";
				variableName = variableName + " == null ? null : java.sql.Time.valueOf(" + variableName + ".toLocalTime())";
			}
			//IFJAVA8_END
				// CLOB support
			else if (types.isAssignable(o, readerType) || types.isAssignable(o, clobType)) {
				method = "Clob";
			} else if (types.isAssignable(o, inputStreamType) || types.isAssignable(o, blobType)) {
				method = "Blob";
			} else if (types.isAssignable(o, fileType)) {
				// todo: does this close the InputStream properly????
				w.write("\t\t\ttry {\n" +
						"\t\t\t\tps.setBlob(" + index + ", new java.io.FileInputStream(" + variableName + "));\n" +
						"\t\t\t} catch (java.io.FileNotFoundException e) {\n" +
						"\t\t\t\tthrow new SQLException(\"File to Blob FileNotFoundException\", e);\n" +
						"\t\t\t}");
				return;
			} else if (types.isAssignable(o, byteArrayType)) {
				method = "Blob";
				variableName = variableName + " == null ? null : new java.io.ByteArrayInputStream(" + variableName + ")";
			} else if (types.isAssignable(o, sqlArrayType)) {
				method = "Array";
			} else {
				// shouldn't get here ever, if we do the types should be more specific
				processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@JdbcMapper.SQL could not properly infer PreparedStatement bind call for param", param);
				return;
			}
		w.write("ps.set");
		w.write(method);
		w.write('(');
		w.write(Integer.toString(index));
		w.write(", ");
		w.write(variableName);
		w.write(");\n");
	}

	private void writeAllParamAnnotations(final Writer w, final VariableElement param) throws IOException {
		// todo: should this always be done for everything? allow turn off, exclude some annotations, or what?
		for(final AnnotationMirror am : param.getAnnotationMirrors())
			w.append(am.toString()).append(' ');
	}

	private static TypeMirror getSqlParser(final JdbcMapper.Mapper mapper) {
		// ridiculous isn't it?
		try {
			mapper.sqlParser();
		} catch (MirroredTypeException mte) {
			return mte.getTypeMirror();
		}
		return null;
	}

	public static Class<?> typeMirrorToClass(final TypeMirror tm) throws ClassNotFoundException {
		if(tm == null)
			return null;
		switch (tm.getKind()) {
			case BOOLEAN:
				return boolean.class;
			case BYTE:
				return byte.class;
			case SHORT:
				return short.class;
			case INT:
				return int.class;
			case LONG:
				return long.class;
			case CHAR:
				return char.class;
			case FLOAT:
				return float.class;
			case DOUBLE:
				return double.class;
			case ARRAY:
				// yuck
				final TypeMirror arrayComponentType = ((ArrayType) tm).getComponentType();
				switch (arrayComponentType.getKind()) {
					case BOOLEAN:
						return boolean[].class;
					case BYTE:
						return byte[].class;
					case SHORT:
						return short[].class;
					case INT:
						return int[].class;
					case LONG:
						return long[].class;
					case CHAR:
						return char[].class;
					case FLOAT:
						return float[].class;
					case DOUBLE:
						return double[].class;
					case ARRAY:
						throw new MapperException("multi-dimensional arrays are not supported");
					default:
						return Class.forName("[L" + arrayComponentType.toString() + ";");
				}
			case DECLARED:
				if (!((DeclaredType) tm).getTypeArguments().isEmpty()) {
					return Class.forName(typeMirrorStringNoGenerics(tm));
				}
				// fallthrough otherwise...
			default:
				return Class.forName(tm.toString());
		}
	}

	public static String typeMirrorStringNoGenerics(final TypeMirror tm) {
		final String classWithGenerics = tm.toString();
		return classWithGenerics.substring(0, classWithGenerics.indexOf('<'));
	}

	public ExecutableElement getCloseMethod(final TypeElement genClass) {
		ExecutableElement ret = null;
		for (final Element methodElement : genClass.getEnclosedElements()) {
			if ((ret = getCloseMethod(methodElement)) != null)
				return ret;
		}
		// superclasses
		final TypeMirror superclass = genClass.getSuperclass();
		if (superclass.getKind() == TypeKind.DECLARED && (ret = getCloseMethod((TypeElement) types.asElement(superclass))) != null)
			return ret;
		// interfaces
		for (final TypeMirror iface : genClass.getInterfaces()) {
			if (iface.getKind() == TypeKind.DECLARED && (ret = getCloseMethod((TypeElement) types.asElement(iface))) != null)
				return ret;
		}
		return ret;
	}

	public static ExecutableElement getCloseMethod(final Element element) {
		return element.getKind() != ElementKind.METHOD ? null : getCloseMethod((ExecutableElement) element);
	}

	public static ExecutableElement getCloseMethod(final ExecutableElement methodElement) {
		return methodElement.getReturnType().getKind() == TypeKind.VOID &&
				methodElement.getSimpleName().toString().equals("close") &&
				methodElement.getParameters().isEmpty() ? methodElement : null;
	}

	public static boolean isPrimitiveInteger(final TypeKind kind) {
		switch(kind) {
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
				return true;
			default:
				return false;
		}
	}
}
