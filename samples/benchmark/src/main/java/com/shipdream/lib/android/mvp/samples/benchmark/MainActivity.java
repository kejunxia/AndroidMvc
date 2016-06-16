/*
 * Copyright 2016 Kejun Xia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shipdream.lib.android.mvc.samples.benchmark;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.shipdream.lib.android.mvc.MvcGraph;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller0;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller1;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller2;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller3;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller4;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller5;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller6;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller7;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller8;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.Controller9;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller0Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller1Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller2Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller3Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller4Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller5Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller6Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller7Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller8Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.controller.internal.Controller9Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service0;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service1;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service2;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service3;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service4;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service5;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service6;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service7;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service8;
import com.shipdream.lib.android.mvc.samples.benchmark.service.Service9;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service0Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service1Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service2Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service3Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service4Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service5Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service6Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service7Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service8Impl;
import com.shipdream.lib.android.mvc.samples.benchmark.service.internal.Service9Impl;
import com.shipdream.lib.poke.ProviderByClassType;
import com.shipdream.lib.poke.exception.ProviderConflictException;

import javax.inject.Inject;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        
        final TextView textDagger10 = (TextView) findViewById(R.id.text_dagger_inject10);

        final TextView textPokePattern10 = (TextView) findViewById(R.id.text_poke_pattern_inject10);

        final TextView textPokeRegistry10 = (TextView) findViewById(R.id.text_poke_registry_inject10);

        final TextView textDagger10x10 = (TextView) findViewById(R.id.text_dagger_inject10x10);

        final TextView textPokePattern10x10 = (TextView) findViewById(R.id.text_poke_pattern_inject10x10);

        final TextView textPokeRegistry10x10 = (TextView) findViewById(R.id.text_poke_registry_inject10x10);

        findViewById(R.id.button_dagger_inject10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Container10 container = new Container10();

                long ts = System.currentTimeMillis();
                ControllerComponent comp = DaggerControllerComponent.builder().controllerModule(new ControllerModule()).build();
                comp.inject(container);
                long elapsed = System.currentTimeMillis() - ts;
                textDagger10.setText(String.format("Dagger inject 10 fields used %dms", elapsed));
            }
        });

        findViewById(R.id.button_dagger_inject10x10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Container10x10 container = new Container10x10();

                long ts = System.currentTimeMillis();
                ControllerComponent comp = DaggerControllerComponent.builder().controllerModule(new ControllerModule()).build();
                comp.inject(container);
                comp.inject((Controller0Impl)container.controller0);
                comp.inject((Controller1Impl)container.controller1);
                comp.inject((Controller2Impl)container.controller2);
                comp.inject((Controller3Impl)container.controller3);
                comp.inject((Controller4Impl)container.controller4);
                comp.inject((Controller5Impl)container.controller5);
                comp.inject((Controller6Impl)container.controller6);
                comp.inject((Controller7Impl)container.controller7);
                comp.inject((Controller8Impl)container.controller8);
                comp.inject((Controller9Impl)container.controller9);

                long elapsed = System.currentTimeMillis() - ts;
                textDagger10x10.setText(String.format("Dagger inject 10x10 nested fields used %dms", elapsed));
            }
        });

        findViewById(R.id.button_poke_registry_inject10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Container10 container = new Container10();

                long ts = System.currentTimeMillis();
                try {
                    MvcGraph graph = new MvcGraph();
                    
                    register(graph, Service0.class, Service0Impl.class);
                    register(graph, Service1.class, Service1Impl.class);
                    register(graph, Service2.class, Service2Impl.class);
                    register(graph, Service3.class, Service3Impl.class);
                    register(graph, Service4.class, Service4Impl.class);
                    register(graph, Service5.class, Service5Impl.class);
                    register(graph, Service6.class, Service6Impl.class);
                    register(graph, Service7.class, Service7Impl.class);
                    register(graph, Service8.class, Service8Impl.class);
                    register(graph, Service9.class, Service9Impl.class);

                    graph.inject(container);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                long elapsed = System.currentTimeMillis() - ts;
                textPokeRegistry10.setText(String.format("Poke inject 10 fields by registry used %dms", elapsed));
            }
        });

        findViewById(R.id.button_poke_registry_inject10x10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Container10x10 container = new Container10x10();

                long ts = System.currentTimeMillis();
                try {
                    MvcGraph graph = new MvcGraph();

                    register(graph, Controller0.class, Controller0Impl.class);
                    register(graph, Controller1.class, Controller1Impl.class);
                    register(graph, Controller2.class, Controller2Impl.class);
                    register(graph, Controller3.class, Controller3Impl.class);
                    register(graph, Controller4.class, Controller4Impl.class);
                    register(graph, Controller5.class, Controller5Impl.class);
                    register(graph, Controller6.class, Controller6Impl.class);
                    register(graph, Controller7.class, Controller7Impl.class);
                    register(graph, Controller8.class, Controller8Impl.class);
                    register(graph, Controller9.class, Controller9Impl.class);
                    register(graph, Service0.class, Service0Impl.class);
                    register(graph, Service1.class, Service1Impl.class);
                    register(graph, Service2.class, Service2Impl.class);
                    register(graph, Service3.class, Service3Impl.class);
                    register(graph, Service4.class, Service4Impl.class);
                    register(graph, Service5.class, Service5Impl.class);
                    register(graph, Service6.class, Service6Impl.class);
                    register(graph, Service7.class, Service7Impl.class);
                    register(graph, Service8.class, Service8Impl.class);
                    register(graph, Service9.class, Service9Impl.class);

                    graph.inject(container);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                long elapsed = System.currentTimeMillis() - ts;
                textPokeRegistry10x10.setText(String.format("Poke inject 10x10 nested fields by registry used %dms", elapsed));
            }
        });

        findViewById(R.id.button_poke_pattern_inject10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Container10 container = new Container10();
                long ts = System.currentTimeMillis();
                try {
                    new MvcGraph().inject(container);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                long elapsed = System.currentTimeMillis() - ts;
                textPokePattern10.setText(String.format("Poke inject 10 fields by pattern used %dms", elapsed));
            }
        });

        findViewById(R.id.button_poke_pattern_inject10x10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Container10x10 container = new Container10x10();

                long ts = System.currentTimeMillis();
                try {
                    new MvcGraph().inject(container);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                long elapsed = System.currentTimeMillis() - ts;
                textPokePattern10x10.setText(String.format("Poke inject 10x10 nested fields by pattern used %dms", elapsed));
            }
        });
    }
    
    private void register(MvcGraph graph, Class type, Class impl) throws ProviderConflictException {
        graph.getRootComponent().register(new ProviderByClassType(type, impl));
    }

    public static class Container10 {
        @Inject
        Service0 service0;
        @Inject
        Service1 service1;
        @Inject
        Service2 service2;
        @Inject
        Service3 service3;
        @Inject
        Service4 service4;
        @Inject
        Service5 service5;
        @Inject
        Service6 service6;
        @Inject
        Service7 service7;
        @Inject
        Service8 service8;
        @Inject
        Service9 service9;
    }

    public static class Container10x10 {
        @Inject
        Controller0 controller0;
        @Inject
        Controller1 controller1;
        @Inject
        Controller2 controller2;
        @Inject
        Controller3 controller3;
        @Inject
        Controller4 controller4;
        @Inject
        Controller5 controller5;
        @Inject
        Controller6 controller6;
        @Inject
        Controller7 controller7;
        @Inject
        Controller8 controller8;
        @Inject
        Controller9 controller9;
    }
}
