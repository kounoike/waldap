const path = require('path');

module.exports = {
  context: path.join(__dirname, "/src/main/assets/js"),
  mode: 'development',
  //mode: 'production',
  entry: './main.js',
  devtool: 'source-map',
  module: {
    rules: [
      {
        test: /\.css$/,
        loader: "style!css"
      },
      {
        test: /(\.sass|\.scss)$/,
        use: [
          {loader: 'style-loader'},
          {loader: 'css-loader'},
          {loader: 'sass-loader'}
        ]
      },
      {
        test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        loader: "file-loader",
        options: {
          publicPath: 'assets/'
        }
        //loader: "url-loader?limit=10000&mimetype=application/font-woff"
      },
      {
        test: /\.(ttf|eot|svg)(\?v=[0-9]\.[0-9]\.[0-9])?$/,
        loader: "file-loader",
        options: {
          publicPath: 'assets/'
        }
      }
    ]
  },
  output: {
    //path: __dirname + '/src/main/webapp/assets',
    path: __dirname + '/target/webapp/assets',
    filename: 'bundle.js'
  }
};
