/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.ark.bootstrap;

import com.alipay.sofa.ark.common.util.FileUtils;
import com.alipay.sofa.ark.spi.constant.Constants;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class IdeLauncher extends ArkLauncher {
    private static final String DOWN_LOAD_SHELL = "download(){\n"
                                                  + "  if [ ! -f ${version_file}'.jar' ]; then\n"
                                                  + "    echo \" start to download serverless-runtime \"${version_file}'.jar'\n"
                                                  + "    curl \"http://mvn.dev.alipay.net/artifactory/content/groups/public/com/alipay/sofa/sofa-serverless-runtime-release/${version}/${version_file}.jar\" -o ${version_file}.jar\n"
                                                  + "    echo \" download serverless-runtime success : \" + `ls -la ${version_file}.jar`\n"
                                                  + "  else\n"
                                                  + "    echo \"serverless-runtime \"${version_file}\".jar already downloaded\"\n"
                                                  + "  fi\n"
                                                  + "}\n"
                                                  + "\n"
                                                  + "unpack(){\n"
                                                  + "  echo \" start to unzip serverless runtime \"${version_file}\n"
                                                  + "  mkdir -p ./.ark-runtime\n"
                                                  + "  rm -rf ./.ark-runtime/\n"
                                                  + "  unzip ${version_file}.jar -d ./.ark-runtime\n"
                                                  + "  ls -la ./.ark-runtime/${version_file}/\n"
                                                  + "  unzip ./.ark-runtime/SOFA-ARK/container/*.jar -d ./.ark-runtime/SOFA-ARK/container/sofa-ark-all\n"
                                                  + "  rm -rf ./.ark-runtime/SOFA-ARK/container/*.jar\n"
                                                  + "  unzip ./.ark-runtime/SOFA-ARK/plugin/*.jar -d ./.ark-runtime/SOFA-ARK/plugin/serverless-runtime-alipay-sofa-boot-plugin\n"
                                                  + "  rm -rf ./.ark-runtime/SOFA-ARK/plugin/*.jar\n"
                                                  + "}\n"
                                                  + "\n"
                                                  + "main(){\n"
                                                  + "  echo $1\n"
                                                  + "  export verison=$1;\n"
                                                  + "  export version_file=\"sofa-serverless-runtime-release-\"$1\n"
                                                  + "  download\n" + "  unpack\n" + "}\n"
                                                  + "main \"$@\"\n";

    public static void main(String[] args) throws Exception {
        initEnv();
        new IdeLauncher().launch(args);
    }

    public static void run(Class clazz, String[] args) throws Exception {
        System.setProperty("Main-Class", clazz.getCanonicalName());
        initEnv();
        new IdeLauncher().launch(args);
    }

    public static void initEnv() throws Exception {
        System.setProperty("start_in_ide", "true");
        System.setProperty(Constants.ENABLE_EXPLODED, "true");
        initRuntime();
    }

    private static void initRuntime() throws Exception {
        String version = System.getProperty("serverless_runtime_version", "3.4.7");
        File runtime = new File(System.getProperty("runtime_path", "./.ark-runtime"));
        if (!runtime.exists()) {
            runtime.mkdir();
            FileWriter fileWriter = new FileWriter("runtime_init.sh");
            fileWriter.write("");
            fileWriter.write(DOWN_LOAD_SHELL);
            fileWriter.flush();
            fileWriter.close();
            System.out.println("start to download server less runtime");
            Process process = Runtime.getRuntime().exec("sh runtime_init.sh " + version);
            System.out.println(eatStream(process.getInputStream()));
            System.out.println(eatStream(process.getErrorStream()));
            process.waitFor(10, TimeUnit.SECONDS);
            process.destroy();
        }

    }

    private static String eatStream(InputStream stream) {
        Scanner scanner = new Scanner(stream);
        try {
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString();
        } finally {
            scanner.close();
        }
    }
}
