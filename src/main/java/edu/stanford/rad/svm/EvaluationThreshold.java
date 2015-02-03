package edu.stanford.rad.svm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class EvaluationThreshold {

	public static void main(String[] args) throws IOException {
		String dataFileFolder = "/Users/saeedhp/Dropbox/Stanford/Tools/libsvm-3.20/Data/ectopicPregnancy/3to1/";
		List<Integer> trueLable = new ArrayList<Integer>();
		List<String> fileName = new ArrayList<String>();
		List<Integer> prediction = new ArrayList<Integer>();
		List<Double> value = new ArrayList<Double>();
		Map<Double, Integer> truelabelMap = new TreeMap<Double, Integer>();
		Map<Double, Integer> predictionMap = new TreeMap<Double, Integer>();
		Map<Double, String> fileNameMap = new TreeMap<Double, String>();


		// Read true labels
		File dataFile = new File(dataFileFolder + "label-test.txt");
		BufferedReader bReader = new BufferedReader(new FileReader(dataFile));
		String line;
		while ((line = bReader.readLine()) != null) {
			if (line.isEmpty())
				continue;
			trueLable.add(Integer.parseInt(line));
		}
		bReader.close();
		
		// Read filenames
		dataFile = new File(dataFileFolder + "fileNames-test.txt");
		bReader = new BufferedReader(new FileReader(dataFile));
		while ((line = bReader.readLine()) != null) {
			if (line.isEmpty())
				continue;
			fileName.add(line);
		}
		bReader.close();

		// Read predictions
		dataFile = new File(dataFileFolder + "output.txt");
		bReader = new BufferedReader(new FileReader(dataFile));
		while ((line = bReader.readLine()) != null) {
			if (line.isEmpty())
				continue;
			prediction.add(Integer.parseInt(line));
		}
		bReader.close();

		// Read values
		dataFile = new File(dataFileFolder + "values.txt");
		bReader = new BufferedReader(new FileReader(dataFile));
		while ((line = bReader.readLine()) != null) {
			if (line.startsWith("Accuracy") || line.isEmpty()) {
				System.out.println(line);
				continue;
			}
			value.add(Double.parseDouble(line));
		}
		bReader.close();

		if (value.size() != prediction.size() || value.size() != trueLable.size() || value.size() != fileName.size()) {
			System.out.println("not equal sizes: v:" + value.size() + " p:" + prediction.size() + " t:" + trueLable.size() + " f:" + fileName.size());
		} else {
			System.out.println("Evaluation size: " + value.size());
		}
		
		List<Integer> tmpPrediction = new ArrayList<Integer>();

		for(int i=0; i<value.size(); ++i){
			truelabelMap.put(value.get(i), trueLable.get(i));
			predictionMap.put(value.get(i), prediction.get(i));
			fileNameMap.put(value.get(i), fileName.get(i));
			tmpPrediction.add(+1);
		}
		List<Integer> sortedTrueLable = new ArrayList<Integer>();
		List<Integer> sortedPrediction = new ArrayList<Integer>();
		List<String> sortedfileName = new ArrayList<String>();

		sortedTrueLable.addAll(truelabelMap.values());
		sortedPrediction.addAll(predictionMap.values());
		sortedfileName.addAll(fileNameMap.values());
		
		PrintWriter pw = new PrintWriter(dataFileFolder + "evaluation-threshold.tsv", "UTF-8");
		pw.printf("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n","file", "trueLabel", "prediction", "threshold","accuracy", "precision", "recall", "f1", "sensitivity", "specificity", "tpr", "fpr");
		
		computeMetrics("", 0, 0, Double.NEGATIVE_INFINITY, sortedTrueLable, tmpPrediction, pw);
		int counter = 0;
		
		for (Entry<Double, Integer> entry : truelabelMap.entrySet()) {
		    double threshold = entry.getKey();
		    int truelabel = sortedTrueLable.get(counter);
		    int pred = sortedPrediction.get(counter);
		    String fname = sortedfileName.get(counter);

		    tmpPrediction.set(counter, -1);
		    computeMetrics(fname, truelabel, pred, threshold, sortedTrueLable, tmpPrediction,pw);
			++counter;
		}
		
		pw.close();
	}
	
	static void computeMetrics(String fname, int truelabel, int pred, double threshold, List<Integer> trueLable, List<Integer> prediction, PrintWriter pw) {
		double tp = 0, fp = 0, tn = 0, fn = 0;
		for (int i = 0; i < prediction.size(); ++i) {
			if (prediction.get(i) == 1) {
				if (trueLable.get(i) == 1) {
					tp++;
				} else {
					fp++;
				}
			} else {
				if (trueLable.get(i) == -1) {
					tn++;
				} else {
					fn++;
				}
			}
		}
		
		double accuracy = (tp + tn) / (tp + tn + fp + fn);
		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn);
		double f1 = 2 * precision * recall / (precision + recall);
		double sensitivity = recall;
		double specificity = tn / (tn + fp);
		double tpr = recall;
		double fpr = fp / (fp + tn);
		
		pw.printf("%s\t%d\t%d\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\t%.4f\n", fname, truelabel, pred, threshold,accuracy, precision, recall, f1, sensitivity,specificity,tpr,fpr);
	}
	
}