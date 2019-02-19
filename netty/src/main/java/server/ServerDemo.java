package server;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author zhenlei
 */
public class ServerDemo {
    private ChannelInitializer channelInitializer;
    private Integer port;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture future;

    private ServerDemo(ChannelInitializer channelInitializer, Integer port) {
        this.channelInitializer = channelInitializer;
        this.port = port;
    }

    public ServerDemo startup() throws InterruptedException {
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                System.out.println(String.format("server start http://127.0.0.1:%d", port));
            }
        });
        return this;
    }

    public void shudown() throws InterruptedException {
        System.out.println(String.format("server shutdown  http://127.0.0.1:%d", port));
        workerGroup.shutdownGracefully().sync();
        bossGroup.shutdownGracefully().sync();
    }

    public static class Builder {
        private ChannelInitializer channelInitializer = null;
        private Integer port = 8080;
        private List<ChannelHandler> handlers = new ArrayList<ChannelHandler>();

        public Builder setChannelInitializer(ChannelInitializer channelInitializer) {
            this.channelInitializer = channelInitializer;
            return this;
        }

        public Builder setPort(Integer port) {
            this.port = port;
            return this;
        }

        public Builder appendHander(ChannelHandler handler) {
            this.handlers.add(handler);
            return this;
        }

        public Builder prependHander(ChannelHandler handler) {
            this.handlers.add(0, handler);
            return this;
        }

        public ServerDemo build() {
            if (Objects.isNull(this.channelInitializer) && this.handlers.size() == 0) {
                throw new RuntimeException("need ChannelInitializer or handlers");
            }
            if (Objects.isNull(this.channelInitializer)) {
                channelInitializer = new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        handlers.forEach(handler -> ch.pipeline().addLast(handler));
                    }
                };
            }
            return new ServerDemo(channelInitializer, port);
        }
    }
}
