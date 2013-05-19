/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class EvaluationExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 1){
			System.err.println("Usage: java " + EvaluationExample.class.getName() + " <PMML file>");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = IOUtil.unmarshal(pmmlFile);

		evaluate(pmml);
	}

	static
	public void evaluate(PMML pmml) throws Exception {
		PMMLManager pmmlManager = new PMMLManager(pmml);

		// Load the default model
		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		Map<FieldName, ?> parameters = readParameters(evaluator);

		FieldName target = evaluator.getTarget();

		Map<FieldName, ?> result = evaluator.evaluate(parameters);

		Object targetValue = EvaluatorUtil.decode(result.get(target));
		System.out.println("Model output: " + targetValue);
	}

	static
	public Map<FieldName, ?> readParameters(Evaluator evaluator) throws IOException {
		Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

		List<FieldName> activeFields = evaluator.getActiveFields();
		System.out.println("Model input " + activeFields.size() + " parameter(s):");

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		try {
			int line = 1;

			for(FieldName activeField : activeFields){
				DataField dataField = evaluator.getDataField(activeField);

				String displayName = dataField.getDisplayName();
				if(displayName == null){
					displayName = activeField.getValue();
				}

				DataType dataType = dataField.getDataType();

				System.out.print(line + ") displayName=" + displayName + ", dataType=" + dataType+ ": ");

				String input = reader.readLine();
				if(input == null){
					throw new EOFException();
				}

				parameters.put(activeField, evaluator.prepare(activeField, input));

				line++;
			}
		} finally {
			reader.close();
		}

		return parameters;
	}
}